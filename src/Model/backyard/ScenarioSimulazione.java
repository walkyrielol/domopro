

package model.backyard;

import Model.domosym.DomoSymApplicationController;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Gianmarco
 */
public class ScenarioSimulazione {
    private String nome;
    private Alloggio planimetria;
    private boolean salvato; 
    private InfoScenario info;
    private ArrayList<DispositivoIntelligente> dispositivi;  
    private UserInfo autore;
    private static ScenarioSimulazione istanza;
    private static ScenarioSimulazione importo;
    
    public static ScenarioSimulazione crea(String nome,UserInfo info){
        return new ScenarioSimulazione(nome,info); 
    }    

    /**
     * se salvato è true sto scaricando lo scenario da aprire che quindi va ad occupare
     * la variabile statica nella classe scenario simulazione. il valore false viene usato per caricare
     * uno scenario al solo fine di importare gli elementi.
     * in questo caso andrà ad occupare la variabile statica import
     * @param info
     * @param salvato
     * @return 
     */
    public static ScenarioSimulazione load(InfoScenario info, boolean salvato){
        ScenarioSimulazione s = new ScenarioSimulazione(info,salvato);   
        s.loadAlloggio();
        s.loadDispositivi();
        return s;        
    }
    
    private ScenarioSimulazione(String nome, UserInfo info){            
        this.nome=nome;
        this.autore = info;
        this.salvato = false; 
        this.dispositivi = new ArrayList<DispositivoIntelligente>(); 
        this.planimetria = new Alloggio();
        this.istanza = this;
        this.info = new InfoScenario(-1,nome,info);
    }
    
    private ScenarioSimulazione(InfoScenario info,boolean salvato){
        this.nome=info.getNome();
        this.autore = info.getAutore();
        this.salvato = true;       
        this.dispositivi = new ArrayList<DispositivoIntelligente>();        
        this.info=info;  
        if(salvato)istanza = this;
        else importo = this;        
    }
    
   
    private ArrayList<String> buildQuerySalva(int id){
        ArrayList<String> s = new ArrayList<String>();         
        s.addAll(this.salvaAlloggio());
        s.addAll(this.salvaDispositivi());        
        return s;
    }
    
    /*
    luoghi, risorse
    */
    private String[] buildQueryLoadAlloggio(){
        String[] query = new String [4];
        query[0] ="SELECT * FROM alloggio WHERE idScenario="+this.info.getId();
        query[1] ="SELECT * FROM piano WHERE idScenario="+this.info.getId();
        query[2] ="SELECT * FROM stanza WHERE idScenario="+this.info.getId();
        query[3] ="SELECT * FROM risorsa WHERE idScenario="+this.info.getId();
        return query;
    }
    
    /*
    risorse,azioni,eventi
    */
    private String[] buildQueryLoadDispositivi(){
        String[] query = new String [6];
        query[0]= "SELECT * FROM dispositivo WHERE idScenario ="+this.info.getId();        
        query[1]= "SELECT * FROM azione WHERE idScenario ="+this.info.getId();   
        query[2]= "SELECT * FROM risorsa WHERE idScenario="+this.info.getId();
        query[3]= "SELECT * FROM relazionedisp WHERE idScenario ="+this.info.getId();       
        query[4]= "SELECT * FROM relazioneaz WHERE idScenario ="+this.info.getId()+" ORDER BY 'posizione' ASC";
        query[5]= "SELECT * FROM utilizzoris WHERE idScenario ="+this.info.getId();              
        return query;
    }
    
    //alloggio, piani, stanze, risorse
    public ArrayList<String> salvaAlloggio(){
        ArrayList<String> query = new ArrayList<String>();
        query.add("INSERT INTO alloggio(idScenario) VALUES ('"+this.info.getId()+"')");
        for(Piano piano: this.getPiani()){
           query.add("INSERT INTO piano(nome,livello,idScenario) VALUES ('"+piano.getNome()+"','"+piano.getLivello()+"','"+this.info.getId()+"')"); 
            for(Stanza stanza:piano.getStanze()){
                query.add("INSERT INTO stanza(nome,nomePiano,idScenario) VALUES ('"+stanza.getNome()+"','"+piano.getNome()+"','"+this.info.getId()+"')");
            }
        }    
        for(Risorsa r: this.planimetria.getRisorseFornite()){
            query.add("INSERT INTO risorsa(nome,limite,limiteTot,giorniRinnovo,idAlloggio,nomePiano,nomeStanza,nomeDisp,idScenario)"
                    + " VALUES ('"+r.getNome()+"','"+r.getLimite()+"','"+r.getLimiteTot()+"','"+r.getGiorniRinnovo()+"',"
                    + "'"+((r.getCollocazione() instanceof Alloggio)? 1 : 0)+"',"
                    + "'"+this.nomePiano(r.getCollocazione())+"',"
                    + "'"+((r.getCollocazione() instanceof Stanza)? ((Stanza)r.getCollocazione()).getNome() : -1)+"',"
                    + "'-1','"+this.info.getId()+"')");                    
        }
        return query;
    }
    
    private String nomePiano(Collocazione c){
        if(c instanceof Piano) return ((Piano)c).getNome();
        else if(c instanceof Stanza) return ((Stanza)c).getPiano().getNome();
        else return "-1";
    }
    
    //disp,azioni,ris,relAzioni,relDisp,utilizzoRis
    private ArrayList<String> salvaDispositivi(){
        ArrayList<String> query = new ArrayList<String>();
        for(DispositivoIntelligente disp : this.dispositivi){
            query.add("INSERT INTO dispositivo (nome,nomeAlloggio,nomePiano,nomeStanza,idScenario) VALUES "
                    + "('"+disp.getNome()+"','"+((disp.getLuogo() instanceof Alloggio)? 1 : 0)+"',"
                    + "'"+((disp.getLuogo() instanceof Piano)? ((Piano)disp.getLuogo()).getNome() : -1)+"',"
                    + "'"+((disp.getLuogo() instanceof Stanza)? ((Stanza)disp.getLuogo()).getNome() : -1)+"',"
                    + "'-1'"
                    + "'"+this.info.getId()+"')");
            //Relazione tra dispositivi
            for(DispositivoIntelligente sottodisp: disp.getSottoDispositivi()){
                query.add("INSERT INTO relazionedisp (nomeCom,nomeSot,idScenario) VALUES"
                    + " ('"+disp.getNome()+"',"
                    + "'"+sottodisp.getNome()+"',"
                    + "'"+this.info.getId()+"')");
            }
            //Azioni
            for(Azione az: disp.getAzioni()){
                query.add("INSERT INTO azione (nome,durata,programmabile,nomeDisp,idScenario) VALUES "
                        + "('"+az.getNome()+"',"
                        + "'"+az.getDurata()+"',"
                        + "'"+az.getProgrammabile()+"',"
                        + "'"+az.getDispositivo().getNome()+"',"
                        + "'"+this.info.getId()+"')");
                //Utilizzo Risorse
                for(Entry<Risorsa,Double> utilizzo: az.getUtilizzoRisorse().entrySet()){
                   Risorsa ris = utilizzo.getKey();
                   double val = utilizzo.getValue();
                   query.add("INSERT INTO utilizzoris (nomeAzione,nomeRis,nomeDisp,val,idScenario) VALUES "
                           + "('"+az.getNome()+"',"
                           + "'"+ris.getNome()+"',"
                           + "'"+disp.getNome()+"',"
                           + "'"+val+"',"
                           + "'"+this.info.getId()+"')");
                }
                //Relazione tra azioni
                int i=0;
                for(Azione sottoaz: az.getSottoAzioni()){
                    query.add("INSERT INTO relazioneaz (nomeAzC,nomeAzS,idScenario,posizione) VALUES "
                           + "('"+az.getNome()+"',"
                           + "'"+sottoaz.getNome()+"',"                          
                           + "'"+this.info.getId()+"',"
                            + "'"+(i++)+"')");
                }
            }
            //Risorse fornite dal dispositivo
            for(Risorsa r: disp.getRisorse()){
                 query.add("INSERT INTO risorsa(nome,limite,lmiteTot,giorniRinnovo,idAlloggio,nomePiano,nomeStanza,nomeDisp,idScenario)"
                    + " VALUES ('"+r.getNome()+"','"+r.getLimite()+"','"+r.getLimiteTot()+"','"+r.getGiorniRinnovo()+"',"
                    + "'0',"
                    + "'-1',"
                    + "'-1',"
                    + "'"+disp.getNome()+"',"     
                    + "'"+this.info.getId()+"')");  
            }
        }
        return query;
    }
    
    
    /*
    popolo con i risultati alloggio
    *Qui si deve creare l'alloggio e quindi caricare gli oggetti che lo compongono, luoghi, risorse) l'operazione
    richiede diverse invocazioni la cui struttura è tuttavia ripetitiva.
    */
    public void loadAlloggio(){
        try {            
            String[] query = this.buildQueryLoadAlloggio();
            //ResultSet[] risultati = new ResultSet[query.length];
            for(int i = 0;i<query.length;i++){
                ResultSet risultati = null;
                try{
                    risultati = BackYardApplicationController.getDBController().executeQuery(query[i]);    
                }catch(Exception e){
                    risultati = DomoSymApplicationController.appCtrl.getDBController().executeQuery(query[i]);    
                }
                if(i==0){
                    this.planimetria = new Alloggio();
                    this.planimetria.getPiani().remove(0);
                }
                if(i==1){
                    while(risultati.next()){                        
                        String nome = risultati.getString("nome");
                        int livello = risultati.getInt("livello");
                        Piano piano = new Piano(nome,livello);                        
                        this.planimetria.addPiano(piano);
                        piano.deleteStanza(piano.getStanze().get(0));                       
                    }
                }
                else if(i==2){
                    while(risultati.next()){
                        String nome = risultati.getString("nome");
                        String nomePiano = risultati.getString("nomePiano");
                        Stanza stanza = new Stanza(nome);
                        for(Piano piano: this.getPiani()){
                            if(piano.getNome().equals(nomePiano)){
                                piano.addStanza(stanza);
                            }
                        }
                    }
                }
                else if(i==3){
                    while(risultati.next()){
                        String nome = risultati.getString("nome");
                        int limite = risultati.getInt("limite");
                        int limitetot = risultati.getInt("limiteTot");
                        int giorniRinnovo = risultati.getInt("giorniRinnovo");
                        boolean alloggio = risultati.getBoolean("idAlloggio");
                        String nomePiano = risultati.getString("nomePiano");
                        String nomeStanza= risultati.getString("nomeStanza");                        
                        Collocazione coll = null;
                        if(alloggio) coll = this.planimetria;
                        else if(this.controllaNomePiano(nome)){
                            for(Piano piano: getPiani()){
                                if(piano.getNome().equals(nomePiano)) coll = (Collocazione) piano;
                            }
                        }
                        else{
                            for(Piano piano: getPiani()){
                                for(Stanza stanza: piano.getStanze()){
                                    if(stanza.getNome().equals(nomeStanza))coll = (Collocazione) stanza;
                                }
                            }
                        }
                        if(coll !=null){                            
                            Risorsa ris = this.creaRisorsa(nome, limite, limitetot, giorniRinnovo, coll);                        
                           // if(cont instanceof Alloggio) ((Alloggio)cont).addRisorsa(ris);
                           // else if(cont instanceof Piano) ((Piano)cont).addRisorsa(ris);
                            //else if(cont instanceof Stanza) ((Stanza)cont).addRisorsa(ris);
                        }
                    }
                }
            }    
        } catch (SQLException ex) {
            System.err.println("Errore nel caricare l'alloggio\n"+ex);
        }        
    }
    
    
        /*
        qui si devono creare i vari dispositivi e caricare gli oggetti a loro collegati(risorse,azioni,eventi)
        l'operazione richiede diverse invocazioni
        */
    /*
    ris,azioni,eventi,disp
    */
    private void loadDispositivi() {
        try{
            String[] query = this.buildQueryLoadDispositivi();            
            for(int i = 0;i<query.length;i++){
                System.out.println(i);
                ResultSet risultati = null;
                try{
                    risultati = BackYardApplicationController.getDBController().executeQuery(query[i]);    
                }catch(Exception e){
                    risultati = DomoSymApplicationController.appCtrl.getDBController().executeQuery(query[i]);    
                }
                while(risultati.next()){
                    if(i==0){
                        //Dispositivi
                        String nome = risultati.getString("nome");
                        String tipo = risultati.getString("tipo");       
                        boolean alloggio = risultati.getBoolean("nomeAlloggio");       
                        String nomePiano =  risultati.getString("nomePiano");    
                        String nomeStanza =  risultati.getString("nomePiano");    
                        if(this.creaDispositivo(nome)){
                            DispositivoIntelligente disp = this.apriDispositivo(nome);
                            //disp.setTipo(tipo);
                            if(alloggio) disp.setDove(planimetria);
                            else if(nomePiano != "-1" && nomeStanza == "-1"){
                                disp.setDove(this.trovaPianoPerNome(nomePiano));
                            }
                            else if(nomePiano !="-1" && nomeStanza != "-1"){
                                for(Stanza stanza: this.richiediDettagliPiano(nomePiano).getStanze()){
                                    if(stanza.getNome().equals(nomeStanza)) disp.setDove(stanza);
                                }
                            }
                        }

                    }        
                    //Azioni
                    else if(i==1){
                        String nome =risultati.getString("nome");
                        int durata = risultati.getInt("durata");
                        boolean prog = risultati.getBoolean("programmabile");
                        String nomeDisp = risultati.getString("nomeDisp");
                        DispositivoIntelligente disp = this.apriDispositivo(nome);
                        disp.creaNuovaAzione(nome);
                        Azione az = disp.richiediDettagliAzione(nome);                    
                        az.setDurata(durata);
                        az.setProgrammabilita(prog);
                    }
                    //Risorse dei dispositivi
                    else if(i==2){
                        while(risultati.next()){
                            String nome = risultati.getString("nome");
                            int limite = risultati.getInt("limite");
                            int limitetot = risultati.getInt("limiteTot");
                            int giorniRinnovo = risultati.getInt("giorniRinnovo");                        
                            String nomeDisp = risultati.getString("nomeDisp");
                            if(!nomeDisp.equals("-1")){                           
                                DispositivoIntelligente disp = this.apriDispositivo(nome);
                                Risorsa ris = this.creaRisorsa(nome, limite, limitetot, giorniRinnovo, (Contesto)disp);
                                disp.addRisorsa(ris);
                            }
                        }
                    }
                    //Sottodisp
                    else if(i==3){
                        String nomeCom=risultati.getString("nomeCom");
                        String nomeSot=risultati.getString("nomeSot");
                        DispositivoIntelligente dispCom = this.apriDispositivo(nomeCom);
                        DispositivoIntelligente dispSot = this.apriDispositivo(nomeSot);
                        if(dispCom != null && dispSot != null ){
                            dispCom.getAsComplesso().addSottoDispositivo(dispSot);                        
                        }
                    }
                    //sottoAz
                    else if(i==4){
                        String nomeAzC=risultati.getString("nomeAzC");
                        String nomeAzS=risultati.getString("nomeAzS");
                        int pos = risultati.getInt("pos");
                        Azione azC = null;
                        Azione azS = null;
                        for(DispositivoIntelligente disp: this.dispositivi){
                            for(Azione az:disp.getAzioni()){
                                if(az.getNome().equals(nomeAzC))azC = az;
                                else if(az.getNome().equals(nomeAzS)) azS = az;
                            }
                        }
                        if(azC != null && azS != null){
                            azC.getAsComplessa().addSottoAzioneToPos(azS, pos);
                        }                    
                    }
                    //Utilizzo Risorse
                    else if(i==5){
                        String nomeAzione = risultati.getString("nomeAzione");
                        String nomeDisp = risultati.getString("nomeDisp");
                        String nomeRisorsa =risultati.getString("nomeRis");
                        int val = risultati.getInt("val");
                        DispositivoIntelligente disp = this.apriDispositivo(nomeDisp);
                        if(disp != null){
                            Azione az = disp.richiediDettagliAzione(nomeAzione);
                            if(az!= null){
                                Risorsa ris = this.getRisorsaDaNome(nomeRisorsa);
                                if(ris != null ) disp.aggiungiUtilizzoRisorsa(az, ris, val);
                            }
                        }                            
                    }  
                }
            }
        }catch (SQLException ex) {
                    Logger.getLogger(ScenarioSimulazione.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    
    public String getNome(){
        return this.nome;
    }
    
    public Alloggio getAlloggio(){
        return planimetria;
    }
    
    public boolean getSalvato(){
        return salvato;
    }
    
    public Risorsa getRisorsaDaNome(String nomeRis){
        ArrayList<Risorsa> ris = new ArrayList<Risorsa>();
        this.planimetria.collectRisorseAccessibili(ris);
        for(DispositivoIntelligente disp: dispositivi){
            ris.addAll(disp.getRisorse());
        }
        for(Risorsa r : ris){
            if(r.getNome().equals(nomeRis)) return r;
        }
        return null;
    }
    
    
    public ArrayList<DispositivoIntelligente> getDispositivi(){
        return dispositivi;
    }
    
    public void setNome(String nome){
        this.nome=nome;
    }
    
    public void setAlloggio(Alloggio alloggio){
        this.planimetria=alloggio;
    }
    
    public void setSalvato(boolean bol){
        this.salvato=bol;
    }    
    
    public boolean salva(){
        if(salvato) return false;
        if(getInfoScenario() != null){
            this.info.elimina();
        }
        String query = info.buildQueryCrea(this.nome,BackYardApplicationController.getAppController().getUtente());        
        int nuovoid = BackYardApplicationController.getDBController().executeInsert(query);
        this.info.setId(nuovoid);
        ArrayList<String> query2 = this.buildQuerySalva(nuovoid);
        for(String s: query2){            
            System.out.println(s);
            BackYardApplicationController.getDBController().executeUpdate(s);
        }
        this.setSalvato(true);
        return true;
    }
   
    
    private Piano trovaPianoPerNome(String nome){
        for(Piano piano: getPiani()){
            if(piano.getNome().equals(nome)) return piano;
        }
        return null;
    }
    
    private boolean controllaNomeDispotivo(String nome){
        return (this.apriDispositivo(nome) != null ? true : false);
    }
    
    private boolean controllaNomePiano(String nome){
        return (trovaPianoPerNome(nome) != null ? true : false);
    }    
    
    /**
     * Controllo che il livello a cui voglio inserire il piano sia disponibile
     * @param lvl
     * @return 
     */
    private boolean controllaLivelloPiano(int lvl){
        for(Piano p: this.planimetria.getPiani()){
            if(lvl == p.getLivello()) return true;
        }
        return false;
    }
    
    /**
     * Controlo se al piano1 esiste già una stanza che si chiama "nome"
     * @param piano1
     * @param nome
     * @return 
     */
    private boolean controllaNomeStanza(Piano piano1,String nome){
        for(Piano piano: getPiani()){
            if(piano1.getNome().equals(piano.getNome())){
                for(Stanza stanza: piano.getStanze())
                    if(stanza.getNome().equals(nome)) return true;
            }
        }
        return false;
    }
    
    /**
     * Controlo se esiste già una risorsa con quel nome
     * @param nome
     * @return 
     */
    private boolean controllaNomeRisorsa(String nome){
        for(Risorsa risorsa: planimetria.getRisorse()){
            if(risorsa.getNome().equals(nome)) return true;
        }
        return false;
    }
    
    public InfoScenario getInfoScenario(){
        return this.info;
    }
    
    public void setInfoScenario(InfoScenario info){
        this.info = info;
    }
    
    /**
     * Prendo la lista dei piani della planimetria
     * @return lista piani 
     */
    public ArrayList<Piano> getPiani(){
        return planimetria == null ? new ArrayList<Piano>() : this.planimetria.getPiani();
    }    
    
    public boolean eliminaDispositivo(String nome) {
        DispositivoIntelligente disp = this.apriDispositivo(nome);
        if(disp == null || disp.isSottoDispositivo())return false;
        this.dispositivi.remove(disp);
        setSalvato(false);
        return true;
    }
    
    public boolean duplicaDispositivo(String nome, String nuovoNome ) {
        DispositivoIntelligente disp = this.apriDispositivo(nome);
        if(this.controllaNomeDispotivo(nuovoNome) && disp != null) return false;
        DispositivoIntelligente dispNuovo = disp.clone();
        dispNuovo.setNome(nuovoNome);
        this.dispositivi.add(dispNuovo);
        setSalvato(false);
        return true;
    }
    
    
    public boolean modificaDispositivo(String nome, String nuovoNome){
        DispositivoIntelligente disp = this.apriDispositivo(nome);
        if(disp == null && this.controllaNomeDispotivo(nuovoNome)) return false;
        disp.setNome(nuovoNome);
        setSalvato(false);
        return true;
    }
    
    public boolean modificaRisorsa(Risorsa risorsa, String nome, int limite) {
        if(!risorsa.getNome().equals(nome) && this.controllaNomeRisorsa(nome)) return false;
        risorsa.setNome(nome);
        risorsa.setlimite(limite);
        setSalvato(false);
        return true;
    }
    
    public boolean modificaRisorsa(Risorsa risorsa, String nome, int limite,int limitetot,int rinnovo){
        if(!risorsa.getNome().equals(nome) && this.controllaNomeRisorsa(nome)) return false;
        risorsa.setNome(nome);
        risorsa.setlimite(limite);
        risorsa.setlimiteTot(limitetot);
        risorsa.setRinnovo(rinnovo);
        setSalvato(false);
        return true;
    }
    
    /**
     * Cerco tutti i luoghi in cui posso collocare un dispositivo
     * @return lista di luoghi
     */
    public ArrayList<Luogo> richiediCollocazionePerDispositivo(){
        return planimetria.getLuoghi();        
    }
    
    /**
     * Cerco tutti i luoghi e i dispositivi in cui posso collocare una risorsa
     * @return 
     */
    public ArrayList<Collocazione> richiediCollocazionePerRisorsa(){
        ArrayList<Collocazione> luoghi=new ArrayList<Collocazione>();
        luoghi.addAll(planimetria.getLuoghi());
        luoghi.addAll(this.dispositivi);
        return luoghi;        
    }
    
    public boolean assegnaCollocazione(DispositivoIntelligente disp,Luogo luogo){
        if(disp != null && luogo != null){
            if(disp.isSpostabile(luogo)){
                disp.setDove(luogo);
                setSalvato(false);
                return true;
            }
        }
        return false;
    }
    
    public boolean assegnaCollocazione(Risorsa risorsa,Collocazione colloc) {
        if(risorsa == null || colloc == null) return false;
        boolean utilizzata= risorsa.isUtilizzata();
        if(!utilizzata){
            risorsa.getCollocazione().removeRisorsa(risorsa);
            colloc.addRisorsa(risorsa);
            risorsa.setCollocazione(colloc);
            setSalvato(false);
        }
        return !utilizzata;
    }
    
    public ArrayList<DispositivoIntelligente> richiediSottoDispositivi(DispositivoIntelligente dispositivo){
        return dispositivo == null ? null :dispositivo.getSottoDispositivi();
    }
    
    /**
     * Lista di tutti i dispositivi che non sono usati da altri dispositivi
     * @param dispositivo
     * @return 
     */
    public ArrayList<DispositivoIntelligente> richiediDispositiviUsabili(DispositivoIntelligente dispositivo){
        ArrayList<DispositivoIntelligente> elenco = new ArrayList<DispositivoIntelligente>();
        for(DispositivoIntelligente d: this.dispositivi){
           if(!d.usa(dispositivo)) elenco.add(d);
        }
        return elenco;
    }
    
    public void aggiungiComponente(DispositivoIntelligente disp,DispositivoIntelligente sottodisp){
        if(!disp.isDIComplesso()){
            disp.getAsComplesso().addSottoDispositivo(sottodisp);            
        }
        else{
            ((DIComplesso)disp).addSottoDispositivo(sottodisp);
        }
        BackYardApplicationController.getAppController().getScenarioCorrente().setSalvato(false);
    }
    
    public boolean eliminaComponente(DispositivoIntelligente disp,DispositivoIntelligente sottoDisp){         
        if(disp.removeSottoDispositivo(sottoDisp)) return false;
        if(disp.getSottoDispositivi().size() ==0){
            this.dispositivi.add(disp.getAsSemplice());
            this.dispositivi.remove(disp);                        
        }
        BackYardApplicationController.getAppController().getScenarioCorrente().setSalvato(false);
        return true;
    }
    
    /**
     * Aggiungo un piano, controllo prima se il livello è disponibile
     * @param piano
     * @return 
     */
    public boolean importaPiano(Piano piano){        
        if(this.controllaLivelloPiano(piano.getLivello())) return false;
        for(Piano p: getPiani()){
            if(p.getNome().equals(piano.getNome())) return false;
        }
        return this.planimetria.addPiano(piano);
    }    
    
    public boolean importaDispositivo(DispositivoIntelligente disp){
        if(this.controllaNomeDispotivo(disp.getNome())) return false;
        this.dispositivi.add(disp);
        return true;
    }
    
    public Piano aggiungiPiano(String nome, int lvl) {
        if(planimetria == null ||nome == null || this.controllaNomePiano(nome) || lvl<0 || this.controllaLivelloPiano(lvl))return null;
        Piano piano = new Piano(nome,lvl);
        this.planimetria.addPiano(piano);
        setSalvato(false);
        return piano;
    }
    
    public boolean eliminaPiano(String nomePiano){
        Piano piano = this.trovaPianoPerNome(nomePiano);
        if(piano == null) return false;
        boolean usato= false;
        for(DispositivoIntelligente disp : this.dispositivi){
            if(disp.inLuogo(piano)) usato= true;
            ArrayList<Luogo> elenco =disp.getLuogo().getLuoghi();
            for(Luogo luogo: elenco){
                 if(disp.inLuogo(luogo)) usato= true;
            }
            usato= false;
        }
        if(!usato){
            this.planimetria.deletePiano(piano);
            setSalvato(false);
        }
        return !usato;
    }
    
    public Piano richiediDettagliPiano(String nome){
        return this.trovaPianoPerNome(nome);
    }
    
    public Stanza apriStanza(Piano p,String nome){
        return p.getStanza(nome);
    }
    
    public boolean modificaPiano(Piano piano,String nome, int lvl) {  
        if(piano == null || lvl < 0 ) return false;
        if(this.controllaNomePiano(nome)&& !(piano.getNome().equals(nome))) return false;
        if(this.controllaLivelloPiano(lvl) && (piano.getLivello()!= lvl))return false ;        
        piano.setNome(nome);
        piano.setLivello(lvl);
        this.setSalvato(false);
        return true;
    }
    
    public Stanza aggiungiStanza(Piano piano,String nomeStanza) {
        if(piano == null || nomeStanza == null || this.controllaNomeStanza(piano, nomeStanza)) return null;
        Stanza stanza = new Stanza(nomeStanza);
        piano.addStanza(stanza);
        setSalvato(false);
        return stanza;       
    }
    
    public boolean modificaStanza(Stanza stanza,String nome) {
        if(stanza == null || nome == "" || nome == null || this.controllaNomeStanza(stanza.getPiano(), nome)) return false;        
        stanza.setNome(nome);   
        setSalvato(false);
        return true;        
    }    
    
    public boolean eliminaStanza(Stanza stanza){
        boolean usata = false;
        for(DispositivoIntelligente disp: this.dispositivi){
            if(disp.inLuogo(stanza)) usata = true;             
        }
        if(!usata){
            Piano piano = stanza.getPiano();
            piano.deleteStanza(stanza);
            setSalvato(false);
            return true;
        }        
        return false;
    }
    
    public boolean spostaStanza(Stanza stanza,Piano nuovoPiano){
        boolean valido = !this.controllaNomeStanza(nuovoPiano, stanza.getNome());
        boolean usata = false;
        for(DispositivoIntelligente disp: this.dispositivi){
            if(disp.inLuogo(stanza)) usata = true;
        }        
        if(!valido || usata) return false;
        else if(valido && !usata){
            Piano piano = stanza.getPiano();
            piano.deleteStanza(stanza);
            nuovoPiano.addStanza(stanza);
            setSalvato(false);
        }
        return true;
    }    
    
    /**
     * Viene creato un elenco di oggetti Risorsa che può contenere: tutte le risorse associate alla planimetria 
     * di s e ai dispositivi di s (se filtrare = falso) oppure solo le risorse associate al contesto (se filtrare = vero)
     * @param contesto
     * @param filtrare
     * @return una lista di risorse
     */
    public ArrayList<Risorsa> richiediRisorseEsistenti(Contesto contesto, boolean filtrare){
        if(filtrare) return contesto.getRisorseFornite();
        ArrayList<Risorsa> risultato = new ArrayList<Risorsa>();
        this.planimetria.collectRisorseAccessibili(risultato);
        for(DispositivoIntelligente disp: this.dispositivi){
            risultato.addAll(disp.getRisorse());
        }
        return risultato;
    }
    
    public Risorsa creaRisorsa(String nome,int limite, Collocazione coll){
        if(this.controllaNomeRisorsa(nome))  return null;
        Risorsa ris = new Risorsa(nome,coll,limite);
        coll.addRisorsa(ris);
        setSalvato(false);
        return ris;
    }
    public Risorsa creaRisorsa(String nome,int limite,int limiteTot,int rinnovo,Collocazione coll){
       if(this.controllaNomeRisorsa(nome)) return null;
       Risorsa ris = new Risorsa(nome,coll,limiteTot,limite,rinnovo);
       coll.addRisorsa(ris);
       setSalvato(false);
       return ris;
    }
    
    public boolean creaDispositivo(String nome) {
        if(!this.controllaNomeDispotivo(nome)) return false;     
        this.dispositivi.add(new DISemplice(nome));
        this.setSalvato(false);
        return true;
    }
    
    public DispositivoIntelligente apriDispositivo(String nome){
        for(DispositivoIntelligente disp: this.dispositivi){
            if(disp.getNome().equals(nome)) return disp;
        }
        return null;
    }
    
    public boolean eliminaRisorsa(Risorsa ris){
        if(ris.usata()) return false;
        else{
            Collocazione c = ris.getCollocazione();
            c.removeRisorsa(ris);
            ris.setCollocazione(null);
            return true;
        }
    }
    
    public String toString(){
        return this.nome;
    }
    
    
    
    
}
