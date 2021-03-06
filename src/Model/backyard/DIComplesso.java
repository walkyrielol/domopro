/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model.backyard;


import static model.backyard.BackYardApplicationController.getAppController;
import java.util.ArrayList;

/**
 *
 * @author Gianmarco
 */
public class DIComplesso extends DispositivoIntelligente {
    private ArrayList<DispositivoIntelligente> sottodispositivi;
    public DIComplesso(String nome) {
        super(nome);        
        sottodispositivi = new ArrayList<DispositivoIntelligente>();
    }
    
    public ArrayList<DispositivoIntelligente> getSottodispositivi(){
        return this.sottodispositivi;
    }
    
    /**
     * Aggiunge all'azione azione la sottoazione sottoAz 
     * @param azione
     * @param sottoAz
     * @return 
     */
    public boolean aggiungiSottoazione(AzioneComplessa azione, Azione sottoAz){        
        if(azione == null || sottoAz == null || !azione.addSottoAzione(sottoAz)) return false;
        BackYardApplicationController.getAppController().getScenarioCorrente().setSalvato(false);
        return true;
    } 
    
    /**
     * Elimina dall'azione az la sottoazione sottoaz 
     * @param az
     * @param sottoaz
     * @return 
     */
    public boolean eliminaSottoazione(AzioneComplessa az, Azione sottoaz){
        if(az == null || sottoaz == null || !az.removeSottoAzione(sottoaz)) return false;
        BackYardApplicationController.getAppController().getScenarioCorrente().setSalvato(false);
        return true;
    }
    
    public boolean modificaPosizione(AzioneComplessa azione, Azione sottoAz, int pos){
        if(azione == null || sottoAz == null || pos <0) return false;
        azione.removeSottoAzione(sottoAz);
        azione.addSottoAzioneToPos(azione, pos);
        BackYardApplicationController.getAppController().getScenarioCorrente().setSalvato(false);
        return true;
    }
    
    public void addSottoDispositivo(DispositivoIntelligente disp){
        if(disp != null)sottodispositivi.add(disp);
        BackYardApplicationController.getAppController().getScenarioCorrente().setSalvato(false);
    }
    
    public boolean removeSottoDispositivo(DispositivoIntelligente disp){
       boolean usato = false;
        for(Azione az: this.getAzioni()){
            if(!az.isMacroazione()) usato = false;
            for(Azione sottoAz: az.getSottoAzioni()){
                if(sottoAz.usaDispositivo(disp)) usato = true;
            }
        }
        if(!usato) return false;
        this.sottodispositivi.remove(disp);
        return true;
    }      

    
    public boolean isDIComplesso(){
        return true;
    }
    
    public ArrayList<DispositivoIntelligente> getSottoDispositivi(){        
        return this.sottodispositivi;
    }
    
    
    
   /**
    * Se disp1 era usato come sottodispositivo di this, bisogna rimpiazzarlo
    * fra i suoi sottodispositivi con la "nuova identità" disp2
    * @param disp1
    * @param disp2 
    */
    public void shallowReplace(DISemplice disp1,DIComplesso disp2){
        for(DispositivoIntelligente disp: this.getSottodispositivi()){
            if(disp.equals(disp1)){
                int index = this.getSottodispositivi().indexOf(disp1);
                this.removeSottoDispositivo(disp1);
                this.addSottoDispositivo(disp2);
            }
        }
    }   
    
    /**
     * Abilita o disabilita l'evento
     * @param evento
     * @param abilitaz 
     */
    public void impostaAbilitazione(Evento evento,Boolean abilitaz){
        evento.setAbilitato(abilitaz);
        getAppController().getScenarioCorrente().setSalvato(false);
    }
    
    public DISemplice getAsSemplice(){        
        DISemplice disp = new DISemplice(this.getNome());
        disp.setTipo(this.getTipo());
        disp.setDove(this.getLuogo());
        for(Azione az: this.getAzioni()){
            az.setDispositivo(disp);
        }
        return disp;       
    }

    @Override
    public DIComplesso getAsComplesso() {
        return this;
    }
    
    
     
                
    
}
