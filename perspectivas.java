import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import javax.imageio.ImageIO;

public class perspectivas {
	public static String _output_folder = "/home/luciano/Imágenes/perspectivas/";
	public static int _output_width = 1600;
	public static int _output_height = 800;
	public static float _angle_left = 30;
	public static float _angle_right = 30;
	public static float _horizon_height = _output_height / 2;
	public static Point _vp_left = new Point();
	public static Point _vp_right = new Point();
	public static Point _origin = new Point(_output_width / 2, _output_height);
	public static int _cube_size = 250;
	public static double _cube_spacing = 0.1;
	public static boolean _show_grid = true;
	
	public static void main(String[] args) {
		Date date = new Date();
		String output_filename = _output_folder + new Timestamp(date.getTime()) + ".jpg";
		BufferedImage output_image = new BufferedImage(_output_width, _output_height, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = output_image.createGraphics();
		int cube_spacing = (int) (_cube_size * _cube_spacing);
		
		graphics.setPaint(new Color(255, 255, 255));
		graphics.fillRect(0, 0, output_image.getWidth(), output_image.getHeight());
		
		_vp_left.setLocation(_origin.getX() - (_horizon_height / Math.tan(Math.toRadians(_angle_left))), _origin.getY() - _horizon_height);
		_vp_right.setLocation(_origin.getX() + (_horizon_height / Math.tan(Math.toRadians(_angle_right))), _origin.getY() - _horizon_height);
		
		System.out.println("Origin: " + _origin.toString());
		System.out.println("VP Izq: " + _vp_left.toString());
		System.out.println("VP Der: " + _vp_right.toString());
		
		if (_show_grid) {
			graphics.setPaint(new Color(180, 180, 180));
			Point from = new Point(_origin);
			for (int i = 0; i < 3; i++) {
				graphics.drawLine((int) from.getX(), (int) from.getY(), (int) _vp_left.getX(), (int) _vp_left.getY());
				graphics.drawLine((int) from.getX(), (int) from.getY(), (int) _vp_right.getX(), (int) _vp_right.getY());				
				from.setLocation(from.getX(), from.getY() - _cube_size);
				graphics.drawLine((int) from.getX(), (int) from.getY(), (int) _vp_left.getX(), (int) _vp_left.getY());
				graphics.drawLine((int) from.getX(), (int) from.getY(), (int) _vp_right.getX(), (int) _vp_right.getY());
				from.setLocation(from.getX(), from.getY() - cube_spacing);
			}		
			graphics.drawLine((int) _origin.getX(), (int) _origin.getY(), (int) from.getX(), (int) from.getY());
		}
		
		Color color = new Color(0, 0, 0);
		
		graphics = drawCube(_origin, _origin, _cube_size, _angle_left, _vp_left, _angle_right, _vp_right, graphics, color);
		graphics = drawCube(_origin, new Point((int)_origin.getX(), (int) _origin.getY() - _cube_size - cube_spacing), _cube_size, _angle_left, _vp_left, _angle_right, _vp_right, graphics, color);
		graphics = drawCube(_origin, new Point((int)_origin.getX(), (int) _origin.getY() - ((_cube_size + cube_spacing) * 2)), _cube_size, _angle_left, _vp_left, _angle_right, _vp_right, graphics, color);
		Point cube_origin = linesIntersection(_origin, _vp_right, new Point((int)_origin.getX() + _cube_size, _output_height), new Point((int)_origin.getX() + _cube_size + cube_spacing, 0));
		Point cube_height = new Point((int)_origin.getX(), (int)_origin.getY() - _cube_size);
		int cube_size = pointsDistance(cube_origin, linesIntersection(cube_height, _vp_right, cube_origin, new Point((int)cube_origin.getX(), 0)));
		graphics = drawCube(_origin, cube_origin, cube_size, _angle_left, _vp_left, _angle_right, _vp_right, graphics, color);

		cube_spacing = (int) (cube_size * _cube_spacing);
		cube_origin = linesIntersection(new Point((int)cube_origin.getX(), (int) cube_origin.getY() - cube_size - cube_spacing), _vp_right, new Point((int)_origin.getX() + _cube_size, _output_height), new Point((int)_origin.getX() + _cube_size + cube_spacing, 0));
		graphics = drawCube(_origin, cube_origin, cube_size, _angle_left, _vp_left, _angle_right, _vp_right, graphics, color);
		
		cube_origin = linesIntersection(new Point((int)cube_origin.getX(), (int) cube_origin.getY() - cube_size - cube_spacing), _vp_right, new Point((int)_origin.getX() + _cube_size, _output_height), new Point((int)_origin.getX() + _cube_size + cube_spacing, 0));
		graphics = drawCube(_origin, cube_origin, cube_size, _angle_left, _vp_left, _angle_right, _vp_right, graphics, color);
				
		try {
			File output_file = new File(output_filename);
			ImageIO.write(output_image, "jpg", output_file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Done!");
	}
	
	private static Graphics2D drawCube(Point origin, Point P1, int size, float angle_left, Point vp_left, float angle_right, Point vp_right, Graphics2D graphics, Color color) {
		graphics.setPaint(color);
		graphics.setPaint(new Color(0, 0, 0));
		
		Point[] cube = new Point[8];
		
		cube[0] = P1;
		cube[4] = new Point((int) cube[0].getX(), (int) cube[0].getY() - size);
		
		// Moving left
		double distanceX = Math.cos(Math.toRadians(_angle_left)) * _cube_size;
		cube[3] = linesIntersection(cube[0], vp_left, new Point((int) (cube[0].getX() - distanceX), (int) cube[0].getY()), new Point((int) (cube[0].getX() - distanceX), 0));
		cube[7] = linesIntersection(cube[4], vp_left, cube[3], new Point((int) cube[3].getX(), 0));
				
		// Moving right
		distanceX = Math.cos(Math.toRadians(angle_right)) * size;
		cube[1] = linesIntersection(cube[0], vp_right, new Point((int) (cube[0].getX() + distanceX), (int) cube[0].getY()), new Point((int) (cube[0].getX() + distanceX), 0));
		cube[5] = linesIntersection(cube[4], vp_right, cube[1], new Point((int) cube[1].getX(), 0));
				
		// Intersection top
		cube[6] = linesIntersection(cube[7], vp_right, cube[5], vp_left);		
		
		// Intersection bottom
		cube[2] = linesIntersection(cube[3], vp_right, cube[1], vp_left);				
		
		// Draw cube
		graphics.drawLine((int) cube[0].getX(), (int) cube[0].getY(), (int) cube[1].getX(), (int) cube[1].getY());
		graphics.drawLine((int) cube[0].getX(), (int) cube[0].getY(), (int) cube[3].getX(), (int) cube[3].getY());
		graphics.drawLine((int) cube[0].getX(), (int) cube[0].getY(), (int) cube[4].getX(), (int) cube[4].getY());
		graphics.drawLine((int) cube[1].getX(), (int) cube[1].getY(), (int) cube[2].getX(), (int) cube[2].getY());
		graphics.drawLine((int) cube[1].getX(), (int) cube[1].getY(), (int) cube[5].getX(), (int) cube[5].getY());
		graphics.drawLine((int) cube[2].getX(), (int) cube[2].getY(), (int) cube[3].getX(), (int) cube[3].getY());
		graphics.drawLine((int) cube[2].getX(), (int) cube[2].getY(), (int) cube[6].getX(), (int) cube[6].getY());
		graphics.drawLine((int) cube[3].getX(), (int) cube[3].getY(), (int) cube[7].getX(), (int) cube[7].getY());
		graphics.drawLine((int) cube[4].getX(), (int) cube[4].getY(), (int) cube[5].getX(), (int) cube[5].getY());
		graphics.drawLine((int) cube[4].getX(), (int) cube[4].getY(), (int) cube[7].getX(), (int) cube[7].getY());
		graphics.drawLine((int) cube[5].getX(), (int) cube[5].getY(), (int) cube[6].getX(), (int) cube[6].getY());
		graphics.drawLine((int) cube[6].getX(), (int) cube[6].getY(), (int) cube[7].getX(), (int) cube[7].getY());		
		
		return graphics;
	}
	
	private static Point linesIntersection(Point P1, Point P2, Point P3, Point P4) {
		double A1 = P2.getY() - P1.getY();
		double B1 = P1.getX() - P2.getX();
		double C1 = A1 * P1.getX() + B1 * P1.getY();
		double A2 = P4.getY() - P3.getY();
		double B2 = P3.getX() - P4.getX();
		double C2 = A2 * P3.getX() + B2 * P3.getY();
		double det = A1 * B2 - A2 * B1;
		
		if (det == 0) {
			return new Point();
		} else {
			double x = (B2 * C1 - B1 * C2) / det;
			double y = (A1 * C2 - A2 * C1) / det;
			
			return new Point((int) x, (int) y);
		}
	}
	
	private static int pointsDistance(Point P1, Point P2) {
		return (int) Math.sqrt(Math.pow(Math.abs(P1.getX() - P2.getX()), 2) + Math.pow(Math.abs(P1.getY() - P2.getY()), 2));
	}
}
