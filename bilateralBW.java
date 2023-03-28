import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class bilateralBW {

    static float euclideanDistance(int x, int y, int i, int j) {
        return (float) Math.sqrt(Math.pow(x - i, 2) + Math.pow(y - j, 2));
    }

    static double gaussian(float distance, double sigma) {
        return Math.exp(-0.5 * Math.pow(distance / sigma, 2));
    }

    private static int[][] convertTo2DUsingGetRGB(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[][] result = new int[height][width];

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                result[row][col] = image.getRGB(col, row) & 0xff;
            }
        }
        return result;
    }

    static void bilateralFilterPixel(int[][] source, int[][] filteredImage, int x, int y, int diameter, double sigmaI, double sigmaS) {
        int height = source.length;
        int width = source[0].length;
        double iFiltered = 0;
        double wP = 0;
        int neighbor_x, neighbor_y;
        int half = diameter / 2;

        // calculate [x][y] value for output
        for (int i = 0; i < diameter; i++) {
            for (int j = 0; j < diameter; j++) {
                neighbor_x = x - (half - i);
                neighbor_y = y - (half - j);
                if (neighbor_x >= 0 && neighbor_x < height && neighbor_y >= 0 && neighbor_y < width) {
                    double gi = gaussian(source[neighbor_x][neighbor_y] - source[x][y], sigmaI); // photometric distance
                    double gs = gaussian(euclideanDistance(x, y, neighbor_x, neighbor_y), sigmaS); // geometric distance
                    double w = gi * gs;
                    iFiltered = iFiltered + source[neighbor_x][neighbor_y] * w;
                    wP = wP + w;
                }
            }
        }
        iFiltered = iFiltered / wP; // normalize
        filteredImage[x][y] = (int) iFiltered;
    }

    static int[][] bilateralFilter(int[][] source, int diameter, double sigmaI, double sigmaS) {
        int[][] filteredImage = new int[source.length][source[0].length];
        int height = source.length;
        int width = source[0].length;

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                bilateralFilterPixel(source, filteredImage, i, j, diameter, sigmaI, sigmaS);
            }
        }
        return filteredImage;
    }

    public static void main(String[] args) throws IOException {
        BufferedImage source = ImageIO.read(new File("dogBW.jpg"));

        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter diameter: ");
        int diameter = Integer.parseInt(scanner.nextLine());
        if (diameter % 2 == 0) {
            System.out.println("diameter must be uneven\n");
            return;
        }
        System.out.println("Enter sigmaI: ");
        int sigmaI = Integer.parseInt(scanner.nextLine());
        System.out.println("Enter sigmaS: ");
        int sigmaS = Integer.parseInt(scanner.nextLine());
        if (sigmaI < 0 || sigmaS < 0) {
            System.out.println("Sigmas must be non-negative\n");
            return;
        }
        
        int[][] src = convertTo2DUsingGetRGB(source);
        int[][] filtered = bilateralFilter(src, diameter, sigmaI, sigmaS);
        int height = source.getHeight();
        int width = source.getWidth();
        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                output.setRGB(j, i, (filtered[i][j] << 16) | (filtered[i][j] << 8) | filtered[i][j]);
            }
        }

        ImageIO.write(output, "jpg", new File("filteredBW.jpg"));
        System.out.println("end");
    }
}