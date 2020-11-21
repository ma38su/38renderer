package renderer;
// 別に必要ない気がする
public class Scene {
	private float[] amb;
	public Scene (float[] amb) {
		this.amb = amb;
	}
	public float[] getAmb () {
		return this.amb;
	}
}
