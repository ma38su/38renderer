package renderer;
import java.awt.Component;
import java.awt.Image;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * *.mqo Reader
 * @since 2005 June
 * @author ma38su
 */
public class MqoReader {

	private Object3D[] obj;
	private Material[] mat;
	private Scene scene;
	
	/** 
	 * mqo ファイルを読み込むためのメソッド
	 * @param objMax 読み込むオブジェクトの最大数
	 * @return 読み込みの成否
	 */
	public Object3D[] getObject3D () {
		return this.obj;
	}
	public Material[] getMaterial () {
		return this.mat;
	}
	public Scene getScene () {
		return this.scene;
	}
	public boolean objRead(Component applet, InputStream in, int objMax) {
		Object3D[] tmpObj = new Object3D[objMax];
		Vertex3D[] vertex = null;
		Face[] tmpFace = null;
		
		// object name {} 内を読み込んでいるフラグ
		// 0 : 初期値
		// 1 : Scene
		// 2 : Material
		// 3 : Object
		short readFLAG = 0;
		
		// 読み込んだオブジェクトの数
		short objCount = 0;
		short matCount = 0;
		short vtCount = 0;
		short fcCount = 0;
		
		// objSwitch
		// 1 : vertex
		// 2 : face
		short objSwitch = 0;
		
		// Object3D Name
		String objName = null;
		BufferedReader bi = null;
		try {
			bi = new BufferedReader(new InputStreamReader(in));
			// read line
			while (bi.ready()) {
				StringTokenizer st = new StringTokenizer(bi.readLine());
				// Object read
				if(st.hasMoreTokens()){
					String tmpToken = st.nextToken();
					if (readFLAG == 1) {
						// Scene {} 内の読み込み
						if (tmpToken.equals("}")) {
							readFLAG = 0;
							continue;
						}
						float[] amb = new float[3];
						if (tmpToken.equals("amb")) {
							amb[0] = Float.valueOf(st.nextToken()).floatValue();
							amb[1] = Float.valueOf(st.nextToken()).floatValue();
							amb[2] = Float.valueOf(st.nextToken()).floatValue();
						}
						this.scene = new Scene(amb);
					} else if (readFLAG == 2) {
						// Material {} 内の読み込み
						if (tmpToken.equals("}")) {
							readFLAG = 0;
							continue;
						}
						String name = tmpToken;
						while (!name.endsWith("\"")) {
							name += " " + st.nextToken();
						}
						// シェーディングのタイプ
						st.nextToken();
						
						// 色関係
						float[] color = new float[4];
						tmpToken = st.nextToken();
						color[0] = Float.valueOf (tmpToken.substring(4, tmpToken.length())).floatValue();
						color[1] = Float.valueOf (st.nextToken()).floatValue();
						color[2] = Float.valueOf (st.nextToken()).floatValue();
						tmpToken = st.nextToken();
						color[3] = Float.valueOf (tmpToken.substring(0, tmpToken.length() - 1)).floatValue();
						float[] shine = new float[5];
						
						// 光関係
						for(int i = 0; i < 5; i++) {
							tmpToken = st.nextToken();
							shine[i] = Float.valueOf (tmpToken.substring(((i == 4)? 6 : 4), tmpToken.length() - 1)).floatValue();
						}
						// テクスチャー
						Image texture = null;
//							if(this.txdir != null && st.hasMoreTokens()) {
//								MediaTracker tracker = new MediaTracker(applet);
//								tmpToken = st.nextToken();
//								texture = Toolkit.getDefaultToolkit().getImage(new URL(this.address + this.txdir + tmpToken.substring(5, tmpToken.length() - 2)));
//								tracker.addImage(texture, 0);
//								try {
//									tracker.waitForAll();
//								} catch (InterruptedException e) {
//									System.out.println("Texture cannot get : "+ e);
//								}
//							}
						// Material作成
						this.mat[matCount++] = new Material(name.substring(1, name.length()-1), color, shine, texture);
					} else if (readFLAG == 3) {
						// Object {}　内の読み込み時
						if (objSwitch == 0){
							if (tmpToken.equals ("}")) {
								readFLAG = 0;
								if (objCount == objMax) break;
							} else if (tmpToken.equals("vertex")){
								int n = Integer.parseInt(st.nextToken());
								// Veretex宣言
								vertex = new Vertex3D[n];
								objSwitch = 1;
							} else if (tmpToken.equals("face")){
								int n = Integer.parseInt(st.nextToken());
								// Face宣言
								tmpFace = new Face[n * 2];
								objSwitch = 2;
							}
						} else if (objSwitch == 1) {
							// Object { vertex {} } 内の読み込み時
							if (tmpToken.equals("}")) {
								objSwitch = 0;
								continue;
							}
							float x = Float.valueOf(tmpToken).floatValue();
							float y = Float.valueOf(st.nextToken()).floatValue();
							float z = Float.valueOf(st.nextToken()).floatValue();
							// --- Vertex追加 ---
							vertex[vtCount] = new Vertex3D(x, y, z);
							vtCount++;
						} else if (objSwitch == 2){
							// Object { face {} } 内の読み込み時
							if (tmpToken.equals("}")) {
								// Vertex Array => Object3D
								vtCount = 0;
								// Face Array　=> Object3D
								Face[] face = new Face[fcCount];
								System.arraycopy (tmpFace, 0, face, 0, fcCount);
								for(int i = 0; i < face.length - 1; i++) {
									for(int j = i + 1; j < face.length; j++) {
										face[i].tangentCheck(face[j]);
									}
								}
								for(int i = 0; i < face.length; i++) {
									face[i].vertexNormalize();
								}
								fcCount = 0;
								tmpObj[objCount++] = new Object3D(objName, vertex, face);
								objSwitch = 0;
								continue;
							}
							int[] v = new int[Integer.parseInt(tmpToken)];
							v[0] = Integer.parseInt (st.nextToken().substring(2));
							for (int i = 1; i < v.length - 1; i++) {
								v[i] = Integer.parseInt (st.nextToken());
							}
							tmpToken = st.nextToken ();
							v[v.length-1] = Integer.parseInt (tmpToken.substring (0, tmpToken.length() - 1));
							if(!st.hasMoreTokens()) continue;
							tmpToken = st.nextToken();
							// Face => Material
							Material mt = null;
							if(tmpToken.startsWith ("M(")) {
								mt = this.mat[Integer.parseInt (tmpToken.substring (2, tmpToken.length() - 1))];
								if(st.hasMoreTokens()) tmpToken = st.nextToken();
							}
							// Face => UV
							int[] puv = null;
							if(tmpToken.startsWith("UV") && mt != null) {
								int texWidth = mt.getTextureWidth() - 1;
								int texHeight = mt.getTextureHeight() - 1;
								// UVマッピングの値は 頂点数×2
								puv = new int[v.length * 2];
								puv[0] = (int)((Float.valueOf(tmpToken.substring(3, tmpToken.length())).floatValue() * texWidth + .5f) * 0x10000);
								for (int i = 1; i < puv.length - 1; i += 1) {
									tmpToken = st.nextToken();
									puv[i]   = (int)((Float.valueOf(tmpToken).floatValue() * ((i % 2 == 0) ? texWidth : texHeight) + .5f) * 0x10000);
								}
								tmpToken = st.nextToken();
								puv[puv.length - 1] = (int)((Float.valueOf(tmpToken.substring(0, tmpToken.length() - 1)).floatValue() * texHeight + .5f) * 0x10000);
							}
							for(int i = 2; i < v.length; i++) {
								Vertex3D a = vertex[v[0]];
								Vertex3D b = vertex[v[i-1]];
								Vertex3D c = vertex[v[i]];
								Vector3D.Float n = a.normal(c, b);
								// --- Face 追加 ---
								int[] uv = null;
								if(puv != null) {
									uv = new int[6];
									uv[0] = puv[0];
									uv[1] = puv[(i-1)*2];
									uv[2] = puv[i*2];
									uv[3] = puv[1];
									uv[4] = puv[(i-1)*2+1];
									uv[5] = puv[i*2+1];
								}
								tmpFace[fcCount++] = new Face (a, b, c, n, Vector3D.gravity(a, b, c), mt, uv);
							}
						}
					} else if (tmpToken.equals ("Scene")) {
						readFLAG = 1;
					} else if (tmpToken.equals ("Material")) {
						int MatNum = Integer.parseInt (st.nextToken());
						this.mat = new Material[MatNum];
						readFLAG = 2;
					} else if (tmpToken.equals ("Object")){
						String name = st.nextToken();
						while (!name.endsWith ("\"")) {
							name += " " + st.nextToken();
						}
						objName = name.substring(1, name.length()-1);
						readFLAG = 3;
					}
				}
			}
		} catch (IOException e) {
			return false;
		} finally {
			if (bi != null) {
				try {
					bi.close ();
				} catch (IOException e) {
					return false;
				}
			}
		}
		this.obj = new Object3D[objCount];
		System.arraycopy (tmpObj, 0, this.obj, 0, objCount);
		if(matCount == 0) this.mat = new Material[0];
		return true;
	}
}