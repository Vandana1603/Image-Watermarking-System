
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import javax.imageio.ImageIO;

public class WatermarkEngine {

    public static void addTextWatermark(
            String text,
            File source,
            File output,
            String user,
            String fontName,
            int size,
            Color color) throws Exception {

        BufferedImage image = ImageIO.read(source);

        Graphics2D g2d = image.createGraphics();

        g2d.setColor(color);
        g2d.setFont(new Font(fontName,Font.BOLD,size));

        int x = image.getWidth()/5;
        int y = image.getHeight()/2;

        g2d.drawString(text,x,y);

        ImageIO.write(image,"png",output);

        g2d.dispose();

        logUsage(user,source.getName());
    }

    private static void logUsage(String user,String file){

        try(FileWriter fw = new FileWriter("logs.txt",true)){

            fw.write(user + "," + file + "," + LocalDateTime.now() + "\n");

        }catch(Exception e){}
    }
}