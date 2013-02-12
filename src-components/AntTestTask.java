public class AntTestTask extends org.apache.tools.ant.Task {
    private String param1;
    public void setParam1(String param1) {
        this.param1 = param1;
    }
    public void execute () throws org.apache.tools.ant.BuildException {
        System.out.println("This is my param1: " + param1);
    }
}
