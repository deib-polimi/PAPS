import java.net.URL;
import java.net.URLClassLoader;

public class Main {

    public static void main(String [] args){
        URL[] urls = ((URLClassLoader) ClassLoader.getSystemClassLoader()).getURLs();
        for (URL url : urls) {
            System.out.println(url);
        }
    }
}
