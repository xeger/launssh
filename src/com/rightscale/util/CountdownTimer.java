/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.rightscale.util;

import java.awt.event.*;
import javax.swing.*;

public class CountdownTimer
        implements Runnable
{
    protected int    _delay;
    protected JLabel _label;
    protected Action _action;
    protected Thread _thread;

    public CountdownTimer(int delay, JLabel label, Action action) {
        _delay  = delay;
        _label  = label;
        _action = action;
    }

    public void start() {
        _thread = new Thread(this);
        _thread.start();
    }

    public void cancel() {
        _thread.interrupt();
    }
    
    public void run() {
        while(_delay > 0) {
            try {
                _label.setText("This window will close automatically in " + _delay + " seconds.");
                _label.getParent().invalidate();
                Thread.sleep(1000);
                _delay--;
            }
            catch(InterruptedException e) {
                break;
            }
        }

        _label.setText("");

        if(_delay == 0) {
            _action.actionPerformed(new ActionEvent(this, 0, "fire"));
        }
    }
}

