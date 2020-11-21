package renderer;
/**
 * マトリクスクラス
 * 
 * @author ma38su
 */
public class Matrix {
	// インスタンス変数
	float m00, m01, m02, m03;
	float m10, m11, m12, m13;
	float m20, m21, m22, m23;
	float m30, m31, m32, m33;
	public Matrix () {
	}
	@Override
	public Matrix clone () {
		Matrix m = new Matrix();
		m.m00 = this.m00; m.m01 = this.m01; m.m02 = this.m02; m.m03 =  0;
		m.m10 = this.m10; m.m11 = this.m11; m.m12 = this.m12; m.m13 =  0;
		m.m20 = this.m20; m.m21 = this.m21; m.m22 = this.m22; m.m23 =  0;
		m.m30 =   0; m.m31 =   0; m.m32 =   0; m.m33 = 1f;
		return m;
	}
	public void fixed () {
		Matrix m = new Matrix ();
		m.m00 = 0x10000; m.m11 = 0x10000; m.m22 = 0x10000; this.m33 = 0x10000;
		this.mult(m);
	}
	// 単位行列に初期化
	public void initUnit () {
		// | 1  0  0|
		// | 0  1  0|
		// | 0  0  1|

		this.m00 = 1; this.m01 = 0; this.m02 = 0; this.m03 = 0;
		this.m10 = 0; this.m11 = 1; this.m12 = 0; this.m13 = 0;
		this.m20 = 0; this.m21 = 0; this.m22 = 1; this.m23 = 0;
		this.m30 = 0; this.m31 = 0; this.m32 = 0; this.m33 = 1;
	}
	/** 行列の掛け算（合成）
	 * 
	 * @param m ... 合成するマトリクス
	 */
	private void mult (Matrix m) {
		float _00 = this.m00 * m.m00 + this.m01 * m.m10 + this.m02 * m.m20 + this.m03 * m.m30;
		float _01 = this.m00 * m.m01 + this.m01 * m.m11 + this.m02 * m.m21 + this.m03 * m.m31;
		float _02 = this.m00 * m.m02 + this.m01 * m.m12 + this.m02 * m.m22 + this.m03 * m.m32;
		float _03 = this.m00 * m.m03 + this.m01 * m.m13 + this.m03 * m.m23 + this.m03 * m.m33;
		
		float _10 = this.m10 * m.m00 + this.m11 * m.m10 + this.m12 * m.m20 + this.m13 * m.m30;
		float _11 = this.m10 * m.m01 + this.m11 * m.m11 + this.m12 * m.m21 + this.m13 * m.m31;
		float _12 = this.m10 * m.m02 + this.m11 * m.m12 + this.m12 * m.m22 + this.m13 * m.m32;
		float _13 = this.m10 * m.m03 + this.m11 * m.m13 + this.m12 * m.m23 + this.m13 * m.m33;
		
		float _20 = this.m20 * m.m00 + this.m21 * m.m10 + this.m22 * m.m20 + this.m23 * m.m30;
		float _21 = this.m20 * m.m01 + this.m21 * m.m11 + this.m22 * m.m21 + this.m23 * m.m31;
		float _22 = this.m20 * m.m02 + this.m21 * m.m12 + this.m22 * m.m22 + this.m23 * m.m32;
		float _23 = this.m20 * m.m03 + this.m21 * m.m13 + this.m22 * m.m23 + this.m23 * m.m33;
		
		float _30 = this.m30 * m.m00 + this.m31 * m.m10 + this.m32 * m.m20 + this.m33 * m.m30;
		float _31 = this.m30 * m.m01 + this.m31 * m.m11 + this.m32 * m.m21 + this.m33 * m.m31;
		float _32 = this.m30 * m.m02 + this.m31 * m.m12 + this.m32 * m.m22 + this.m33 * m.m32;
		float _33 = this.m30 * m.m03 + this.m31 * m.m13 + this.m32 * m.m23 + this.m33 * m.m33;
		
		this.m00 = _00; this.m01 = _01; this.m02 = _02; this.m03 = _03;
		this.m10 = _10; this.m11 = _11; this.m12 = _12; this.m13 = _13;
		this.m20 = _20; this.m21 = _21; this.m22 = _22; this.m23 = _23;
		this.m30 = _30; this.m31 = _31; this.m32 = _32; this.m33 = _33;
	}
	
	/**
	 * @param aspect スクリーン縦横比
	 * @param fov 視野角
	 * @param n 
	 * @param f 
	 * @param view 視点スクリーン間の距離
	 * @param cam 原点からカメラ(スクリーン)までの位置
	 */
	public void perspective (float aspect, float fov, float n, float f) {
		
		float w = aspect * (float)(Math.cos(fov * 0.5f) / Math.sin(fov * 0.5f));
		float h = 1.0f   * (float)(Math.cos(fov * 0.5f) / Math.sin(fov * 0.5f));
		float q = f / (f - n);
		Matrix m = new Matrix();
		m.m00 = w; m.m01 = 0; m.m02 =   0; m.m03 = 0;
		m.m10 = 0; m.m11 = h; m.m12 =   0; m.m13 = 0;
		m.m20 = 0; m.m21 = 0; m.m22 =   q; m.m23 = 1;
		m.m30 = 0; m.m31 = 0; m.m32 =-n*q; m.m33 = 0;
		this.mult(m);
	}

	/** x軸回転
	 * 
	 * @param r ...回転角（単位ラジアン）
	 */
	public void rotateX (float r) {
		float sinX = (float)Math.sin(r);
		float cosX = (float)Math.cos(r);	
		// x軸回転
		// | 1     0    0 0|
		// | 0  cosX sinX 0|
		// | 0 -sinX cosX 0|
		// | 0     0    0 1|	

		Matrix m = new Matrix();
		m.m00 = 1; m.m01 =    0; m.m02 =    0; m.m03 = 0;
		m.m10 = 0; m.m11 = cosX; m.m12 = sinX; m.m13 = 0;
		m.m20 = 0; m.m21 =-sinX; m.m22 = cosX; m.m23 = 0;
		m.m30 = 0; m.m31 =    0; m.m32 =    0; m.m33 = 1;

		//行列の合成
		this.mult(m);
	}
	/** y軸回転
	 * 
	 * @param r ...回転角（単位ラジアン）
	 */
	public void rotateY (float r) {
		float sinY = (float)Math.sin(r);
		float cosY = (float)Math.cos(r);
		// y軸回転
		// | cosY 0 -sinY 0|
		// |    0 1     0 0|
		// | sinY 0  cosY 0|
		// |    0 0     0 1|

		Matrix m = new Matrix();
		m.m00 = cosY; m.m01 = 0; m.m02 =-sinY; m.m03 = 0;
		m.m10 =    0; m.m11 = 1; m.m12 =    0; m.m13 = 0;
		m.m20 = sinY; m.m21 = 0; m.m22 = cosY; m.m23 = 0;
		m.m30 =    0; m.m31 = 0; m.m32 =    0; m.m33 = 1;

		//行列の合成
		this.mult(m);
	}
	/** z軸回転
	 * 
	 * @param r ...回転角（単位ラジアン）
	 */
	public void rotateZ (float r) {
		float sinZ = (float)Math.sin(r);
		float cosZ = (float)Math.cos(r);	
		// z軸回転
		// |  cosZ sinZ 0 0|
		// | -sinZ cosZ 0 0|
		// |     0    0 1 0|
		// |     0    0 0 1|	

		Matrix m = new Matrix();
		m.m00 = cosZ; m.m01 = sinZ; m.m02 = 0; m.m03 = 0;
		m.m10 =-sinZ; m.m11 = cosZ; m.m12 = 0; m.m13 = 0;
		m.m20 =    0; m.m21 =    0; m.m22 = 1; m.m23 = 0;
		m.m30 =    0; m.m31 =    0; m.m32 = 0; m.m33 = 1;
		//行列の合成
		this.mult(m);
	}
	public void scale (float sx,float sy,float sz) {
		// 拡大縮小
		// | sx  0  0|
		// |  0 sy  0|
		// |  0  0 sz|

		Matrix m = new Matrix();
		m.m00 = sx; m.m01 =  0; m.m02 =  0; m.m03 = 0;
		m.m10 =  0; m.m11 = sy; m.m12 =  0; m.m13 = 0;
		m.m20 =  0; m.m21 =  0; m.m22 = sz; m.m33 = 1;
		m.m30 =  0; m.m31 =  0; m.m32 =  0; m.m33 = 1;
		//行列の合成
		this.mult(m);
	}
	public void toView () {
		System.out.println(this.m00+" "+this.m01+" "+this.m02+" "+this.m03);
		System.out.println(this.m10+" "+this.m11+" "+this.m12+" "+this.m13);
		System.out.println(this.m20+" "+this.m21+" "+this.m22+" "+this.m23);
		System.out.println(this.m30+" "+this.m31+" "+this.m32+" "+this.m33);
	}
	/**
	 * ベクトルの一次変換
	 * @param v3D 
	 * @param a 変換したベクトルを渡すベクトル
	 * @param b 変換するベクトル
	 */
	public void transform (Vector3D.Float v3D) {
		float vx = this.m00 * v3D.getX() + this.m10 * v3D.getY() + this.m20 * v3D.getZ() + this.m30;
		float vy = this.m01 * v3D.getX() + this.m11 * v3D.getY() + this.m21 * v3D.getZ() + this.m31;
		float vz = this.m02 * v3D.getX() + this.m12 * v3D.getY() + this.m22 * v3D.getZ() + this.m32;
		v3D.setLocation(vx, vy, vz);
	}
	public void transform (Vertex3D v3D) {
		int vx = (int)(this.m00 * v3D.getX() + this.m10 * v3D.getY() + this.m20 * v3D.getZ() + this.m30);
		int vy = (int)(this.m01 * v3D.getX() + this.m11 * v3D.getY() + this.m21 * v3D.getZ() + this.m31);
		int vz = (int)(this.m02 * v3D.getX() + this.m12 * v3D.getY() + this.m22 * v3D.getZ() + this.m32);
		v3D.setLocation(vx, vy, vz);
	}
	/** 平行移動
	 * 
	 * @param d ... 平行移動量{x軸方向, y軸方向, z軸方向}
	 */
	public void translate (int[] d) {
		Matrix m = new Matrix ();
		m.m00 =   1f; m.m01 =    0; m.m02 =    0; m.m03 =  0;
		m.m10 =    0; m.m11 =   1f; m.m12 =    0; m.m13 =  0;
		m.m20 =    0; m.m21 =    0; m.m22 =   1f; m.m23 =  0;
		m.m30 = d[0]; m.m31 = d[1]; m.m32 = d[0]; m.m33 = 1f;
		this.mult(m);
	}
	public void viewport () {
		Matrix m = new Matrix();
		m.m00 =   1f; m.m01 =    0; m.m02 =    0; m.m03 =  0;
		m.m10 =    0; m.m11 =  -1f; m.m12 =    0; m.m13 =  0;
		m.m20 =    0; m.m21 =    0; m.m22 =   1f; m.m23 =  0;
		m.m30 =    0; m.m31 =    0; m.m32 =    0; m.m33 = 1f;
		this.mult(m);
	}
}