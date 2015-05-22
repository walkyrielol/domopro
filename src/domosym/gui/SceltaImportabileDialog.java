/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package domosym.gui;

import backyard.gui.*;
import javax.swing.JOptionPane;
import javax.swing.ListModel;

/**
 *
 * @author picardi
 */
public class SceltaImportabileDialog extends javax.swing.JDialog {

	private int value;
	/**
	 * Creates new form NomeScenarioDialog
	 */
	public SceltaImportabileDialog(java.awt.Frame parent) {
		super(parent, true);
		initComponents();
		value = JOptionPane.CANCEL_OPTION;
	}

	public Object getSelectedObject()
	{
		return importabiliList.getSelectedValue();
	}
	
	public int getValue()
	{
		return this.value;
	}
	
	public void setImportabili(ListModel lm)
	{
		this.importabiliList.setModel(lm);
		this.importabiliList.clearSelection();
	}
	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
        javax.swing.JPanel jPanel1 = new javax.swing.JPanel();
        javax.swing.JScrollPane jScrollPane1 = new javax.swing.JScrollPane();
        importabiliList = new javax.swing.JList();
        jPanel2 = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel1.setText("Scegli un elemento da importare:");
        jLabel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 0, 5));
        getContentPane().add(jLabel1, java.awt.BorderLayout.PAGE_START);

        jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel1.setLayout(new java.awt.BorderLayout());

        importabiliList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(importabiliList);

        jPanel1.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        okButton.setText("Ok");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });
        jPanel2.add(okButton);

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        jPanel2.add(cancelButton);

        getContentPane().add(jPanel2, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        if (importabiliList.getSelectedIndex() < 0)
		{
			JOptionPane.showMessageDialog(this, "Devi scegliere un elemento", 
				"Errore", JOptionPane.ERROR_MESSAGE);
			return;			
		}
		this.value = JOptionPane.OK_OPTION;
		this.setVisible(false);
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
		this.value = JOptionPane.CANCEL_OPTION;
		this.setVisible(false);
    }//GEN-LAST:event_cancelButtonActionPerformed

	/**
	 * @param args the command line arguments
	 */
	public static void main(String args[]) {
		/* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
		 * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
		 */
		try {
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (ClassNotFoundException ex) {
			java.util.logging.Logger.getLogger(SceltaImportabileDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (InstantiationException ex) {
			java.util.logging.Logger.getLogger(SceltaImportabileDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			java.util.logging.Logger.getLogger(SceltaImportabileDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (javax.swing.UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(SceltaImportabileDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		}
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

		/* Create and display the dialog */
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				SceltaImportabileDialog dialog = new SceltaImportabileDialog(new javax.swing.JFrame());
				dialog.addWindowListener(new java.awt.event.WindowAdapter() {
					@Override
					public void windowClosing(java.awt.event.WindowEvent e) {
						System.exit(0);
					}
				});
				dialog.setVisible(true);
			}
		});
	}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JList importabiliList;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JButton okButton;
    // End of variables declaration//GEN-END:variables
}
