package mmlib4j.descriptors.shapeContext;

import mmlib4j.gui.WindowImages;
import mmlib4j.images.BinaryImage;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.AbstractImageFactory;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.images.impl.MmlibImageFactory;
import mmlib4j.segmentation.ThresholdGlobal;
import mmlib4j.utils.ImageBuilder;


/**
 * Project: Computer Vision Framework
 * 
 * @author Wonder Alexandre Luz Alves
 * @advisor Ronaldo Fumio Hashimoto
 * 
 * @date 01/09/2007
 * 
 * @description
 * Classe que implementa o algoritmo canny edger detecter 
 */
public class Canny {

    private float smx[][]; /* Convolucao da imagem com a gaussiana na direcao X */
    private float smy[][]; /* Convolucao da imagem com a gaussiana na direcao Y */
    private float dx[][];  /* Convolucao da imagem com ruido com a derivada na X */
    private float dy[][];  /* Convolucao da imagem com ruido com a derivada na Y */
    private float high; /* Menor valor para threshold */
    private float low;  /* Maior valor para threshold */
    private GrayScaleImage imgMag; /* Imagem das magnitudes */
    private GrayScaleImage imgOut; /* Imagem de saida */
    private int width; /* Largura da imagem */
    private int height; /* Altura da imagem */
 

    /**
     * Inicializacao do algoritmo
     * @param img - imagem de entrada
     * @param s - sigma da funcao de Gauss
     * @param low - menor valor para threshold
     * @param high - maior valor para threshold
     */
    public void init(GrayScaleImage img, float s, float low, float high) {
        this.low = low;
        this.high = high;
        this.width = img.getWidth();
        this.height = img.getHeight();
        
        this.imgOut = img.duplicate();
        
        /* Cria estrutura de armazenamento das magnitudes */
        this.imgMag =  ImageFactory.createGrayScaleImage(AbstractImageFactory.DEPTH_8BITS, img.getWidth(), img.getHeight());
        
        /* Convolucao da imagem com a gaussiana nas direcoes X e Y */
        smx = new float[this.width][this.height];
        smy = new float[this.width][this.height];
        
        /* Convolucao da imagem com ruido com a derivada na direcoes X e Y */
        dx = new float[this.width][this.height];
        dy = new float[this.width][this.height];
    }

    /**
     * Aplica o detector de bordas de Canny na imagem imgOut
     * @param img - imagem de entrada
     * @param s - sigma da funcao de Gauss
     * @param low - menor valor para threshold
     * @param high - maior valor para threshold
     */
    public BinaryImage run(GrayScaleImage img, float s, float low, float high) {
        init(img, s, low, high);
        int size = 0;
        float dgau[] = new float[20];
        float gau[] = new float[20];
        
        /* Cria o filtro da gaussiana e a derivada do filtro da gaussiana */
        for (int i = 0; i < 20; i++) {
            gau[i] = gauss((float) i, s);
            if (gau[i] < 0.0005) {
                size = i;
                break;
            }
            dgau[i] = dGauss((float) i, s);
        }
        
        seperableConvolution(gau, size);

        dxySeperableConvolution(dgau, size);
        
        /*for (int i = 0; i < imgOut.getWidth(); i++){
            for (int j = 0; j < imgOut.getHeight(); j++){
                imgOut.setPixel(i, j, imgMag.getPixel(i, j));
            }
        }*/

        /* Supressao nao maximal - pixels da aresta devem ser um maximo local */
        nonmaxSuppress();
        
        /* Histerese dos pixels da aresta */
        hysteresis();
        
        WindowImages.show(imgMag, imgOut);
        
        return ThresholdGlobal.upperSet(imgOut, 200);
    }

    

    /**
     * Aplica a convolucao da imagem imgOut com uma
     * mascara de tamanho width contendo a funcao de gauss nas direcoes X e Y
     */
    protected void seperableConvolution(float mask[], int size) {
        int k, i1, i2;
        float x, y;

        for (int i = 0; i < width; i++){
            for (int j = 0; j < height; j++) {
                x = mask[0] * imgOut.getPixel(i, j);
                y = mask[0] * imgOut.getPixel(i, j);
                for (k = 1; k < size; k++) {
                    i1 = (i + k) % width;
                    i2 = (i - k + width) % width;
                    y += mask[k] * imgOut.getPixel(i1, j) + mask[k] * imgOut.getPixel(i2, j);
                    i1 = (j + k) % height;
                    i2 = (j - k + height) % height;
                    x += mask[k] * imgOut.getPixel(i, i1) + mask[k] * imgOut.getPixel(i, i2);
                }
                smx[i][j] = x;
                smy[i][j] = y;
            }
        }
          
    }

    /**
     * Aplica a convolucao da imagem imgOut com uma mascara de tamanho width contendo a primeira derivada da funcao de
     * gauss nas direcoes X e Y
     */
    protected void dxySeperableConvolution(float mask[], int size) {
        int k, i1, i2;
        float x, y;
        for (int i = 0; i < width; i++){
            for (int j = 0; j < height; j++) {
                x = 0;
                y = 0;
                for (k = 1; k < size; k++) {
                    i1 = (i + k) % width;
                    i2 = (i - k + width) % width;
                    y += -mask[k] * smy[i1][j] + mask[k] * smy[i2][j];
                    i1 = (j + k) % height;
                    i2 = (j - k + height) % height;
                    x += -mask[k] * smx[i][i1] + mask[k] * smx[i][i2];
                }
                dx[i][j] = x;
                dy[i][j] = y;
            }
        }
    }

    /**
     * Realiza a supresao nao maximal
     */
    protected void nonmaxSuppress() {
        int i, j;
        float xx, yy, g2, g1, g3, g4, g, xc, yc;

        for (i = 1; i < width - 1; i++) {
            for (j = 1; j < height - 1; j++) {
                /* Trata as derivadas x e y como componentes de um vetor */
                xc = dx[i][j];
                yc = dy[i][j];
                g = normalize(xc, yc);

                if (Math.abs(yc) > Math.abs(xc)) {
                    /*
                     * A compontente Y e a maior, entao a direcao do
                     * gradiente e basicamente CIMA/BAIXO
                     */
                    xx = Math.abs(xc) / Math.abs(yc);
                    yy = 1f;

                    g2 = normalize(dx[i - 1][j], dy[i - 1][j]);
                    g4 = normalize(dx[i + 1][j], dy[i + 1][j]);
                    if (xc * yc > 0.0) {
                        g3 = normalize(dx[i + 1][j + 1], dy[i + 1][j + 1]);
                        g1 = normalize(dx[i - 1][j - 1], dy[i - 1][j - 1]);
                    } else {
                        g3 = normalize(dx[i + 1][j - 1], dy[i + 1][j - 1]);
                        g1 = normalize(dx[i - 1][j + 1], dy[i - 1][j + 1]);
                    }

                } else {
                    /*
                     * A compontente X e a maior, entao a direcao do
                     * gradiente e basicamente ESQ/DIR
                     */
                    xx = Math.abs(yc) / Math.abs(xc);
                    yy = 1f;

                    g2 = normalize(dx[i][j + 1], dy[i][j + 1]);
                    g4 = normalize(dx[i][j - 1], dy[i][j - 1]);
                    if (xc * yc > 0.0) {
                        g3 = normalize(dx[i - 1][j - 1], dy[i - 1][j - 1]);
                        g1 = normalize(dx[i + 1][j + 1], dy[i + 1][j + 1]);
                    } else {
                        g1 = normalize(dx[i - 1][j + 1], dy[i - 1][j + 1]);
                        g3 = normalize(dx[i + 1][j - 1], dy[i + 1][j - 1]);
                    }
                }
                /* Agora determina se o pixel atual e maximo */
                if ((g > (xx * g1 + (yy - xx) * g2)) && (g > (xx * g3 + (yy - xx) * g4))) {
                    if (g <= 255)
                        imgMag.setPixel(i, j, (int) g);
                    else
                        imgMag.setPixel(i, j, 255);
                } else {
                    imgMag.setPixel(i, j, 0);
                }
            }
        }
    }

    /**
     * hystheresis: Realiza o que Canny chamou de histerese na imagem.
     * 
     * 1) Marca todas as bordas com magnitude maior que high como corretas. 2)
     * Percorre todos os pixels com magnitude de borda E[low, high]. 3) Se um
     * pixel estao conectado a outro ja marcado como borda, marca-o tambem.
     */
    protected void hysteresis() {
        int i, j;

        /* Limpa a matriz da imagem */
        for (i = 0; i < width; i++)
            for (j = 0; j < height; j++)
                imgOut.setPixel(i, j, 255);

        
        /*
         * Traca arestas ao longo de todos os caminhos cujos valores da
         * magnitude mantem se acima do valor de menor threshold
         */
        for (i = 0; i < width; i++)
            for (j = 0; j < height; j++)
                if (imgMag.getPixel(i, j) >= high)
                    trace(i, j);
    }

    /**
     * trace: Funcao auxiliar para o passo da histerese
     */
    protected boolean trace(int i, int j) {
        int n, m;
        boolean flag = false;

        if (imgOut.getPixel(i, j) == 255) {
            imgOut.setPixel(i, j, 0);
            for (n = -1; n <= 1; n++) {
                for (m = -1; m <= 1; m++) {
                    if (i == 0 && m == 0)
                        continue;
                    if (imgMag.isPixelValid(i + n, j + m) && imgMag.getPixel(i + n, j + m) >= low)
                        if (trace(i + n, j + m)) {
                            flag = true;
                            break;
                        }
                }
                if (flag)
                    break;
            }
            return true;
        }
        return false;
    }
    
    /**
     * Retorna o valor da funcao de gauss
     * @param x
     * @param sigma
     */
    public static float gauss(float x, float sigma) {
        if (sigma == 0)
            return 0;
        return (float) Math.exp((double) ((-x * x) / (2 * sigma * sigma)));
    }

    /**
     * Retorna a primeira derivada da funcao de gauss
     * @param x
     * @param sigma
     */
    public static float dGauss(float x, float sigma) {
        return -x / (sigma * sigma) * gauss(x, sigma);
    }
    
    
    /**
     * Dist�ncia Euclidiana
     * @param x
     * @param y
     * @return
     */
    public static float normalize(double x, double y) {
        return (float) Math.sqrt(x * x + y * y);
    }
    
    
    public static void main(String args[]){
		GrayScaleImage img = ImageBuilder.openGrayImage();
		WindowImages.show(new Canny().run(img, 1, 5, 20));
    }
    
}
