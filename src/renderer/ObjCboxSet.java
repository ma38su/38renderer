package renderer;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
/**
 * オブジェクトの可視設定のチェックボックスのリスナ
 * @author ma38su
 */
public class ObjCboxSet implements ItemListener {
	final ModelCanvas CANVAS;
	final int index;

	/**
	 * @param canvas 
	 * @param n 操作するオブジェクトのインデックス
	 */
	public ObjCboxSet(ModelCanvas canvas, int n) {
		this.CANVAS = canvas;
		this.index = n;
	}
	@Override
	public void itemStateChanged(ItemEvent e) {
		this.CANVAS.setVisibleObject (this.index, e.getStateChange() == ItemEvent.SELECTED);
	}
}