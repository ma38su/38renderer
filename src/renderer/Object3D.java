package renderer;
/**
 * - Object3D class -
 *  + Face
 *  + Vertex
 * @author ma38su
 */
public class Object3D {

	public String name;
	private Vertex3D[] vertex;
	private Face[] face;

	/** コンストラクタ
	 * 
	 * @param n オブジェクト名
	 * @param vt 頂点配列
	 * @param vtlink 頂点配列(面とリンク)
	 * @param fc 面配列
	 */
	public Object3D (String n, Vertex3D[] vt, Face[] fc){
		this.name = n;
		this.vertex = vt;
		this.face = fc;
	}
	/** 頂点配列を取得
	 * 
	 * @return　頂点配列
	 */
	public Vertex3D[] getVertices () {
		return this.vertex;
	}
	/** 面配列を取得
	 * 
	 * @return 面配列
	 */
	public Face[] getFaces () {
		return this.face;
	}
	/** 面配列の長さを取得
	 * 
	 * @return 面配列の長さ
	 */
	public int faceSize () {
		return this.face.length;
	}
}