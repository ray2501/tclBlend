// File ActionListener.java

package tests.javabind;

import java.util.EventListener;

// This interface is used in Bind.java

public interface ActionListener extends EventListener {
    public void actionPerformed(ActionEvent e);
}
