package renderer;

public class Vertex3D extends Vector3D {
	// 固定小数点による位置ベクトル
	int cx, cy, cz;
	// コンストラクタ
	public Vertex3D (float x, float y, float z) {
		super(x, y, z);
	}
	public Vertex3D (Vector3D v) {
		super(v);
	}
	public Vertex3D () {
	}
	public int getTransformedX() { return this.cx; }
	public int getTransformedY() { return this.cy; }
	public int getTransformedZ() { return this.cz; }
	public void setLocation (int x, int y, int z) {
		this.cx = x;
		this.cy = y;
		this.cz = z;
	}
}
