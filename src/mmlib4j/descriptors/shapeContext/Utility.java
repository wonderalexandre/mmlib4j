package mmlib4j.descriptors.shapeContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Utility {

	public static List<Point> sample(List<Point> allpts, int num) {
	    ArrayList<Point> points = new ArrayList<Point>();
		int n, end, aux;
		
	    int v[] = new int[allpts.size()];
	    for(int i = 0; i < v.length; i++) {
	        v[i] = i;
	    }
	    end = allpts.size();

	    points.clear();
	    for (int i = 0; i < num; i++) {
	        n = (int) Math.random() % end;
	        points.add(allpts.get(v[n]));
	        aux = v[n];
	        v[n] = v[end-1];
	        v[end-1] = aux;
	        end--;
	    }
	    return points;
	}
	
	

	//*****************************************************************************/
	//* Calcula a distância entre todos os pares de pontos e devolve a mediana
	//*****************************************************************************/
	public static double median (List<Point> points) {
	    if (points.size() == 0)
	        return 0;

	    int i, j;
	    double m;
	    int num = points.size();
	    List<Double> distance = new ArrayList<Double>();
	    double dist;
	    Point point;

	    for (i = 0; i < num; i++) {
	        point = points.get(i);
	        for (j = i; j < num; j++) {
	            dist = Math.hypot(points.get(j).x - point.x, points.get(j).y - point.y);
	            distance.add(dist);
	        }
	    }
	    Collections.sort(distance);
	    m = distance.get(distance.size()/2);
	    return (m);
	}

	

	//*****************************************************************************/
	//* Devolve a tangente associada à configuração de pixels dada pela imagem img
	//* Deve ser substituido para um metodo mais robusto
	//*****************************************************************************/
	public static Tangent tangent(boolean p[]) {
		Tangent v = new Tangent();
	    v.x = 0;
	    v.y = 0;
	    
	    boolean p11 = p[0];// (img.GetGreen(0,0) == 0);
	    boolean p12 = p[1];//(img.GetGreen(0,1) == 0);
	    boolean p13 = p[2];//(img.GetGreen(0,2) == 0);
	    boolean p21 = p[3];//(img.GetGreen(1,0) == 0);
	    boolean p22 = p[4];//(img.GetGreen(1,1) == 0);
	    boolean p23 = p[5];//(img.GetGreen(1,2) == 0);
	    boolean p31 = p[6];//(img.GetGreen(2,0) == 0);
	    boolean p32 = p[7];//(img.GetGreen(2,1) == 0);
	    boolean p33 = p[8];//(img.GetGreen(2,2) == 0);
	    
	    
	    //************************************/
	    //* 0
	    //************************************/
	    if (!p11 && !p12 && !p13 && p21 & p22 & p23 & !p31 & !p32 & !p33) {
	        v.x = 1; v.y = 0;
	    }
	    //************************************/
	    //* 20
	    //************************************/
	    else if (!p11 && !p12 && p13 && p21 & p22 & p23 & !p31 & !p32 & !p33) {
	        v.x = 0.939692621; v.y = 0.342020143;
	    }
	    else if (!p11 && p12 && p13 && p21 & p22 & !p23 & !p31 & !p32 & !p33) {
	        v.x = 0.939692621; v.y = 0.342020143;
	    }
	    else if (!p11 && !p12 && !p13 && p21 & p22 & p23 & p31 & !p32 & !p33) {
	        v.x = 0.939692621; v.y = 0.342020143;
	    }
	    else if (!p11 && !p12 && !p13 && !p21 & p22 & p23 & p31 & !p32 & !p33) {
	        v.x = 0.939692621; v.y = 0.342020143;
	    }
	    else if (!p11 && !p12 && !p13 && p21 & p22 & !p23 & !p31 & p32 & p33) {
	        v.x = 0.939692621; v.y = 0.342020143;
	    }
	    else if (!p11 && !p12 && !p13 && p21 & p22 & !p23 & !p31 & !p32 & p33) {
	        v.x = 0.939692621; v.y = 0.342020143;
	    }
	    else if (p11 && !p12 && !p13 && !p21 & p22 & p23 & !p31 & !p32 & p33) {
	        v.x = 0.939692621; v.y = 0.342020143;
	    }
	    //************************************/
	    //* 45
	    //************************************/
	    else if (!p11 && !p12 && p13 && !p21 & p22 & !p23 & p31 & !p32 & !p33) {
	        v.x = 0.707106781; v.y = 0.707106781;
	    }
	    else if (!p11 && p12 && !p13 && p21 & p22 & !p23 & !p31 & !p32 & !p33) {
	        v.x = 0.707106781; v.y = 0.707106781;
	    }
	    else if (!p11 && !p12 && p13 && !p21 & p22 & p23 & p31 & p32 & !p33) {
	        v.x = 0.707106781; v.y = 0.707106781;
	    }
	    else if (!p11 && !p12 && p13 && !p21 & p22 & !p23 & p31 & p32 & !p33) {
	        v.x = 0.707106781; v.y = 0.707106781;
	    }
	    //************************************/
	    //* 70
	    //************************************/
	    else if (!p11 && p12 && p13 && !p21 & p22 & !p23 & p31 & p32 & !p33) {
	        v.x = 0.342020143; v.y = 0.939692621;
	    }
	    else if (!p11 && p12 && !p13 && !p21 & p22 & !p23 & p31 & p32 & !p33) {
	        v.x = 0.342020143; v.y = 0.939692621;
	    }
	    else if (!p11 && !p12 && p13 && !p21 & p22 & p23 & !p31 & p32 & !p33) {
	        v.x = 0.342020143; v.y = 0.939692621;
	    }
	    //************************************/
	    //* 90
	    //************************************/
	    else if (!p11 && p12 && !p13 && !p21 & p22 & !p23 & !p31 & p32 & !p33) {
	        v.x = 0; v.y = 1;
	    }
	    else if (p11 && !p12 && !p13 && p21 & p22 & !p23 & p31 & !p32 & !p33) {
	        v.x = 0; v.y = 1;
	    }

	    else if (!p11 && !p12 && p13 && !p21 & p22 & p23 & !p31 & !p32 & p33) {
	        v.x = 0; v.y = 1;
	    }
	    else if (p11 && !p12 && !p13 && !p21 & p22 & !p23 & p31 & !p32 & !p33) {
	        v.x = 0; v.y = 1;
	    }
	    else if (!p11 && p12 && !p13 && !p21 & p22 & p23 & !p31 & p32 & !p33) {
	        v.x = 0; v.y = 1;
	    }
	    //************************************/
	    //* 110
	    //************************************/
	    else if (!p11 && p12 && !p13 && !p21 & p22 & p23 & !p31 & !p32 & p33) {
	        v.x = -0.342020143; v.y = 0.939692621;
	    }
	    else if (p11 && !p12 && !p13 && p21 & p22 & p23 & !p31 & !p32 & !p33) {
	        v.x = -0.342020143; v.y = 0.939692621;
	    }
	    else if (p11 && !p12 && !p13 && !p21 & p22 & !p23 & !p31 & p32 & !p33) {
	        v.x = -0.342020143; v.y = 0.939692621;
	    }
	    else if (p11 && !p12 && !p13 && p21 & p22 & !p23 & !p31 & p32 & !p33) {
	        v.x = -0.342020143; v.y = 0.939692621;
	    }
	    else if (p11 && !p12 && !p13 && p21 & p22 & !p23 & !p31 & !p32 & p33) {
	        v.x = -0.342020143; v.y = 0.939692621;
	    }
	    //************************************/
	    //* 135
	    //************************************/
	    else if (p11 && !p12 && !p13 && !p21 & p22 & !p23 & !p31 & !p32 & p33) {
	        v.x = -0.707106781; v.y = 0.707106781;
	    }
	    else if (p11 && p12 && !p13 && !p21 & p22 & p23 & !p31 & !p32 & p33) {
	        v.x = -0.707106781; v.y = 0.707106781;
	    }
	    else if (p11 && !p12 && !p13 && p21 & p22 & p23 & !p31 & !p32 & p33) {
	        v.x = -0.707106781; v.y = 0.707106781;
	    }
	    else if (p11 && !p12 && !p13 && p21 & p22 & !p23 & !p31 & p32 & p33) {
	        v.x = -0.707106781; v.y = 0.707106781;
	    }
	    else if (!p11 && !p12 && !p13 && p21 & p22 & !p23 & !p31 & p32 & !p33) {
	        v.x = -0.707106781; v.y = 0.707106781;
	    }
	    else if (p11 && p12 && !p13 && !p21 & p22 & p23 & p31 & !p32 & p33) {
	        v.x = -0.707106781; v.y = 0.707106781;
	    }
	    //************************************/
	    //* 160
	    //************************************/
	    else if (p11 && p12 && !p13 && !p21 & p22 & p23 & !p31 & !p32 & !p33) {
	        v.x = -0.939692621; v.y = 0.342020143;
	    }
	    else if (p11 && !p12 && !p13 && p21 & p22 & p23 & !p31 & !p32 & !p33) {
	        v.x = -0.939692621; v.y = 0.342020143;
	    }
	    else if (!p11 && !p12 && !p13 && p21 & p22 & !p23 & !p31 & p32 & p33) {
	        v.x = -0.939692621; v.y = 0.342020143;
	    }
	    else if (!p11 && !p12 && !p13 && p21 & p22 & !p23 & !p31 & !p32 & p33) {
	        v.x = -0.939692621; v.y = 0.342020143;
	    }

	    return v;
	}

}
