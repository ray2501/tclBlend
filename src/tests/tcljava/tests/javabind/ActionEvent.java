// File ActionEvent.java

package tests.javabind;

import java.util.EventObject;

// This interface is used in Bind.java

public class ActionEvent extends EventObject {
    ActionEvent(Object source) {
	super(source);
    }
}
