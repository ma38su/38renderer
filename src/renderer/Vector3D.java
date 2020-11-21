package renderer;
/**
 * ベクトルクラス
 * @author ma38su
 */
public class Vector3D {
	/**
	 * 外積
	 * 
	 * @param a ベクトル
	 * @param b ベクトル
	 * @return 外積
	 */
	public static Vector3D outerProduct(Vector3D a, Vector3D b) {
		// 外積は主に面に垂直な法線ベクトルを求めるために使用する。
		float vx = a.y * b.z - a.z * b.y;
		float vy = a.z * b.x - a.x * b.z;
		float vz = a.x * b.y - a.y * b.x;
		return new Vector3D (vx, vy, vz);
	}
	/** 重心ベクトル
	 * 
	 * @param a ベクトル
	 * @param b ベクトル
	 * @param c ベクトル
	 * @return 重心ベクトル
	 */
	public static Vector3D gravity (Vector3D a, Vector3D b, Vector3D c) {
		float gx = (a.x + b.x + c.x) / 3;
		float gy = (a.y + b.y + c.y) / 3;
		float gz = (a.z + b.z + b.y) / 3;
		return new Vector3D (gx, gy, gz);
	}
	// インスタンス変数
	float x, y, z;
	// コンストラクタ
	public Vector3D () {
	}
	public Vector3D (Vector3D v) {
		this.x = v.x;
		this.y = v.y;
		this.z = v.z;
	}
	public Vector3D (float vx, float vy, float vz) {
		this.x = vx;
		this.y = vy;
		this.z = vz;
	}
	/** インスタンス変数更新
	 * 
	 * @param vx
	 * @param vy
	 * @param vz
	 */
	public void setLocation (float vx, float vy, float vz) {
		this.x = vx;
		this.y = vy;
		this.z = vz;
	}
	public void setLocation (Vector3D v) {
		this.setLocation (v.x, v.y, v.z);
	}
	public void add (Vector3D v) {
		this.x += v.x;
		this.y += v.y;
		this.z += v.z;
	}
	public float getX() {
		return this.x;
	}
	public float getY() {
		return this.y;
	}
	public float getZ() {
		return this.z;
	}
	// 単位ベクトル化
	public void normalize() {
		//単位ベクトルとは長さが１のベクトルのこと。
		this.scale (1f / (float)Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z));
	}
	/**
	 * 内積を求める
	 * @param v 内積を求めるベクトル
	 * @return 内積
	 */
	public float dotProduct (Vector3D v) {
		//内積を取るとベクトルとベクトルのなす角度が求まる。
		return (this.x * v.x + this.y * v.y + this.z * v.z);
	}
	/** 法線ベクトル
	 * メソッドを呼び出したインスタンスを視点とし、a×b
	 * @param a ベクトルaの終点
	 * @param b ベクトルbの終点
	 * @return 法線ベクトル
	 */
	public Vector3D.Float normal (Vector3D a, Vector3D b) {
		float ax = a.x - this.x;
		float ay = a.y - this.y;
		float az = a.z - this.z;
		float bx = b.x - this.x;
		float by = b.y - this.y;
		float bz = b.z - this.z;
		float nx = ay * bz - az * by;
		float ny = az * bx - ax * bz;
		float nz = ax * by - ay * bx;
		Vector3D.Float v = new Vector3D.Float (nx, ny, nz);
		v.normalize();
		return v;
	}
	/** 法線ベクトルを計算するベクトル
	 * 呼び出したベクトルに求めた法線ベクトルを渡す
	 * 
	 * @param a
	 * @param b 
	 * @param c
	 */
	public void normal (Vector3D a, Vector3D b, Vector3D c) {
		float bx = b.x - a.x;
		float by = b.y - a.y;
		float bz = b.z - a.z;
		float cx = c.x - a.x;
		float cy = c.y - a.y;
		float cz = c.z - a.z;
		float nx = by * cz - bz * cy;
		float ny = bz * cx - bx * cz;
		float nz = bx * cy - by * cx;
		this.setLocation (nx, ny, nz);
		this.normalize();
	}
	/** 拡大縮小
	 * 
	 * @param n 倍率
	 */
	public void scale (int n) {
		this.x *= n;
		this.y *= n;
		this.z *= n;
	}
	/** 拡大縮小
	 * 
	 * @param n 倍率
	 */
	public void scale (float n) {
		this.x *= n;
		this.y *= n;
		this.z *= n;
	}
	public static class Float extends Vector3D {
		// 固定小数点による位置ベクトル
		float cx, cy, cz;
		// コンストラクタ
		public Float() { super(); }
		public Float(float x, float y, float z) { super(x, y, z); }
		public Float(Vector3D v3D) { super(v3D); }
		
		public float getConvertedX() { return this.cx; }
		public float getConvertedY() { return this.cy; }
		public float getConvertedZ() { return this.cz; }
		@Override
		public void setLocation (float x, float y, float z) {
			this.cx = x;
			this.cy = y;
			this.cz = z;
		}
		public Vector3D.Float normal (Vertex3D a, Vertex3D b, Vertex3D c) {
			float bx = (float)(b.cx - a.cx) / 0x10000;
			float by = (float)(b.cx - a.cy) / 0x10000;
			float bz = (float)(b.cz - a.cz) / 0x10000;
			float cx = (float)(c.cx - a.cx) / 0x10000;
			float cy = (float)(c.cy - a.cy) / 0x10000;
			float cz = (float)(c.cz - a.cz) / 0x10000;
			float nx = by * cz - bz * cy;
			float ny = bz * cx - bx * cz;
			float nz = bx * cy - by * cx;
			Vector3D.Float v = new Vector3D.Float (nx, ny, nz);
			v.normalize();
			return v;
		}
		/**
		 * 内積を求める
		 * @param v 内積を求めるベクトル
		 * @return 内積
		 */
		public float dotProduct2 (Vector3D v) {
			//内積を取るとベクトルとベクトルのなす角度が求まる。
			return (this.cx * v.x + this.cy * v.y + this.cz * v.z);
		}
	}
}