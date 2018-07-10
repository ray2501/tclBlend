import java.awt.*;

public class packDemo {

  public static void main(String[] args) {
      
    Button mid = new Button("Mid");
    Button bot = new Button("Bot");
    Button right = new Button("Right");

    Frame f = new Frame();
    
    f.setSize(200,200);
    
    f.setLayout(new PackerLayout());
    
    f.add("-side right -fill y -expand false -anchor n", right);

    f.add("-side bottom -fill both -expand true -anchor s -padx 10 -pady 10", bot);
    
    f.add("-anchor center", mid);
    
    f.show();
    
  }

}
