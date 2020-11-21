package renderer;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

public class Controller implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener {
	
	@Override
	public void keyTyped(KeyEvent e) {}
	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_SHIFT) Controller.shiftKey(true);
	}
	@Override
	public void keyReleased(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_SHIFT) Controller.shiftKey(false);
	}
	private static boolean shift;
	/** シフトキーの状態を更新
	 * 
	 * @param state KeyDownのときtrue;
	 */
	public static void shiftKey (boolean state) {
		Controller.shift = state;
	}
	private final ModelCanvas CANVAS;
	// マウスポインタの座標
	private int[] p = new int[3];
	private int[] dp = new int[3];
	// 押されたボタン
	private int button;
	
	public Controller (ModelCanvas canvas) {
		this.CANVAS = canvas;
	}
	@Override
	public void mouseDragged(MouseEvent e) {
		// 変化角度を計算
		this.dp[0] = e.getX() - this.p[0];
		this.dp[1] = e.getY() - this.p[1];
		// 変化させた時点の角度を保存
		this.p[0] = e.getX();
		this.p[1] = e.getY();

		if(this.button == MouseEvent.BUTTON3 || this.button == MouseEvent.BUTTON1) {
			// 回転移動
			if(shift) this.dp[1] = 0;
			this.CANVAS.setAngle(this.dp);
		} else if (this.button == MouseEvent.BUTTON2) {
			// 平行移動
			this.CANVAS.setPosition(this.dp);			
		}
	}
	@Override
	public void mouseMoved(MouseEvent e) {
	}
	@Override
	public void mouseClicked(MouseEvent e) {
	}
	@Override
	public void mouseEntered(MouseEvent e) {
	}
	@Override
	public void mouseExited(MouseEvent e) {
	}
	@Override
	public void mousePressed(MouseEvent e) {
		this.button = e.getButton();
		this.p[0] = e.getX();
		this.p[1] = e.getY();
	}
	@Override
	public void mouseReleased(MouseEvent e) {
	}
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		int amount = e.getWheelRotation();
		this.CANVAS.setScale(amount);
	}
}