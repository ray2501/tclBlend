package tests.signature;

public class NullSignature {
  public NullSignature() {}

  public String  primitive_call(int i) {return "int";}
  public String  primitive_call(float f) {return "float";}

  public String  object_call(Integer i) {return "Integer";}
  public String  object_call(Float f) {return "Float";}
  public String  object_call(Float[] f) {return "Float[]";}

  public String  combined_call(int i) {return "int";}
  public String  combined_call(float f) {return "float";}
  public String  combined_call(Integer i) {return "Integer";}
  public String  combined_call(Float f) {return "Float";}

  public Object  getNullAsObject() {return null;}
  public String  getNullAsString() {return null;}
  public Integer getNullAsInteger() {return null;}
  public Float   getNullAsFloat() {return null;}
}
