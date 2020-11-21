package renderer;
/**
 * 面(三角形)クラス
 * @author ma38su
 */
public class Face {
	private static float smoothLimit = 2f;
	public static void setSmoothing (float limit) {
		smoothLimit = limit;
	}
	// -- インスタンス変数 --
	// 面の頂点の位置ベクトル
	private Vertex3D[] vertexLocate = new Vertex3D[3];
	// 面の法線ベクトル
	private Vector3D.Float faceNormal;
	// 頂点の法線ベクトル
	private Vector3D.Float[] vertexNormal = new Vector3D.Float[3];
	private int[] tangentCount = new int[3];
	// ポリゴンの位置(重心による)
	private Vector3D gravity;
	// 材質
	private Material mat;
	// UV
	private int[] uv;
	private boolean smoothFLAG = false;
	
	/**
	 * 
	 * @param v0 頂点の位置ベクトル
	 * @param v1 頂点の位置ベクトル
	 * @param v2 頂点の位置ベクトル
	 * @param nv 面法線ベクトル
	 * @param gv 面の重心ベクトル
	 * @param mt 面の材質
	 * @param uv 面のUV情報(頂点に載せたほうがいいかも)
	 */
	public Face(Vertex3D v0, Vertex3D v1, Vertex3D v2, Vector3D.Float nv, Vector3D gv, Material mt, int[] uv) {
		this.vertexLocate[0] = v0;
		this.vertexLocate[1] = v1;
		this.vertexLocate[2] = v2;
		for(int i = 0; i < 3; i++) {
			this.vertexNormal[i] = new Vector3D.Float(nv);
			this.tangentCount[i]++;
		}
		this.faceNormal = nv;
		this.gravity = gv;
		this.mat = (mt == null)? this.mat = new Material() : mt;
		this.uv = uv;

	}
	/** 
	 * 接面をチェックしてcos(smoothLimit)以下であれば、共有頂点に面法線ベクトルを加算
	 * @param f 比較する頂点
	 */
	public void tangentCheck (Face f) {
		if(smoothLimit <= this.faceNormal.dotProduct(f.faceNormal)) {
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					if(this.vertexLocate[i].equals(f.vertexLocate[j])) {
						// 互いの面の頂点法線ベクトルを加算
						this.vertexNormal[i].add(f.faceNormal);
						this.tangentCount[i]++;
						f.vertexNormal[j].add(this.faceNormal);
						f.tangentCount[j]++;
						// 法線ベクトルを加算した面はスムーズシェーディングを行うことを示す
						this.smoothFLAG = true;
						f.smoothFLAG = true;
						break;
					}
				}
			}
		}
	}
	// 頂点の法線ベクトルの正規化
	public void vertexNormalize() {
		for(int i = 0; i < 3; i++) {
			if(this.tangentCount[i] > 0) {
				this.vertexNormal[i].scale(1f / this.tangentCount[i]);
			}
		}
	}
	public boolean normal() {
		return this.faceNormal.getConvertedZ() >= 0;
	}
	public Vector3D.Float getFaceNormal () {
		return this.faceNormal;
	}
	public Vector3D.Float getNormal(int index) {
		return this.vertexNormal[index];
	}
	public Vector3D getGravity () {
		return this.gravity;
	}
	public Material getMaterial () {
		return this.mat;
	}
	public Vertex3D[] getVertices () {
		return this.vertexLocate;
	}
	public Vertex3D getVertices (int n) {
		return this.vertexLocate[n];
	}
	public int[] getUV () {
		return this.uv;
	}
	public boolean smoothRequest () {
		return this.smoothFLAG;
	}
}