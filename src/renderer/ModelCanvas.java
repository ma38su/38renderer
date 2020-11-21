package renderer;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.MemoryImageSource;

import javax.swing.JPanel;

/**
 * - ModelPanel class -
 * 
 * @author ma38su
 */
public final class ModelCanvas extends JPanel {

	private static final long serialVersionUID = 1L;

	/**
	 * モデルオブジェクト配列
	 */
	private final Object3D[] object;

	/**
	 * オブジェクトの可視情報
	 */
	private final boolean[] visibleObject;

	/**
	 * キャンバス幅
	 */
	private final int CANVAS_WIDTH;

	/**
	 * キャンバス高さ
	 */
	private final int CANVAS_HEIGHT;

	/**
	 * 背景色
	 */
	private final int BGCOLOR;

	/**
	 * 光源ベクトル
	 */
	private final Vector3D light = new Vector3D(1f, -2f, 1f);

	/**
	 * スクリーンバッファ
	 */
	private final Image IMG;

	private final MemoryImageSource MIS;
	private final int[] xMin, xMax;
	private final int[] minZ, maxZ;
	private final int[] minU, maxU, minV, maxV;
	private final int[] minR, maxR, minG, maxG, minB, maxB;
	private final int[] zbuf, pbuf;

	/**
	 * repaint()制御のためのフラグ (newPixels()対策)
	 */
	private boolean repaintFLAG;

	/**
	 * benchmark を取るためのフラグ
	 */
	private boolean benchmarkFLAG;

	// benchmark 関係
	private long spf, time = System.currentTimeMillis();
	private float time0, time1, time2, time3, fps;
	private float initiTime, modelTime, imageTime, totalTime;
	private int loop = 30, count;

	/**
	 * 軸の表示フラグ
	 */
	private boolean axisFLAG = true;

	/**
	 * 軸の色 {x,y,z}
	 */
	private final int[] AXIS_COLOR = { 0xff000000, 0xff000000, 0xff000000 };

	/**
	 * 最大倍率
	 */
	private float SCALE_MAX = 10f;

	/**
	 * 最小倍率
	 */
	private float SCALE_MIN = 0.05f;

	/**
	 * 回転の制御 {x, y, z}
	 */
	private final boolean[] ANGLE_CONTROL = new boolean[3];

	/**
	 * 回転の最大角 {x, y, z}
	 */
	private final float[] ANGLE_MAX;

	/**
	 * 回転の最小角 {x, y, z}
	 */
	private final float[] ANGLE_MIN;

	/**
	 * マウス入力の感度 {回転, ホイール}
	 */
	private final float[] SENSE;

	/**
	 * 倍率
	 */
	private float scale;

	/**
	 * 回転角
	 */
	private final float[] angle;

	/**
	 * 平行移動量
	 */
	private final int[] position;

	public ModelCanvas(Object3D[] obj, Material[] mat, int cw, int ch, float s,
			int bg, boolean[] flag, float[] sense) {

		// オブジェクト
		this.object = obj;
		this.visibleObject = new boolean[obj.length];
		for (int i = 0; i < this.visibleObject.length; i++)
			this.visibleObject[i] = true;

		// 視点設定
		this.ANGLE_MIN = new float[3];
		this.ANGLE_MAX = new float[3];
		this.scale = s;
		this.angle = new float[3];
		this.position = new int[3];
		this.position[0] = cw / 2;
		this.position[1] = ch / 2;
		// Frame info
		this.CANVAS_WIDTH = cw;
		this.CANVAS_HEIGHT = ch;
		this.BGCOLOR = bg;

		this.SENSE = sense;
		this.benchmarkFLAG = flag[0];

		this.xMin = new int[ch];
		this.xMax = new int[ch];
		this.minZ = new int[ch];
		this.maxZ = new int[ch];
		this.minU = new int[ch];
		this.maxU = new int[ch];
		this.minV = new int[ch];
		this.maxV = new int[ch];
		this.minR = new int[ch];
		this.maxR = new int[ch];
		this.minG = new int[ch];
		this.maxG = new int[ch];
		this.minB = new int[ch];
		this.maxB = new int[ch];

		this.zbuf = new int[cw * ch];
		this.pbuf = new int[cw * ch];

		this.MIS = new MemoryImageSource(this.CANVAS_WIDTH, this.CANVAS_HEIGHT,
				this.pbuf, 0, this.CANVAS_WIDTH);

		// Multi Frame Animation
		this.MIS.setAnimated(true);
		// Frame Buffered
		this.IMG = this.createImage(this.MIS);

		// 回転角制御設定
		this.control(1, -(float) Math.PI / 2, (float) Math.PI / 2);
	}

	public void reload() {
		this.repaintFLAG = true;
		this.repaint();
	}

	@Override
	public void update(Graphics g) {
		if (this.repaintFLAG || this.benchmarkFLAG) {
			this.repaintFLAG = false;
			this.paint(g);
		}
		if (this.benchmarkFLAG) {
			g.drawString(
					("fps " + this.fps + ", spf " + this.spf + ", flame " + this.count),
					5, 15);
			g.drawString(("バッファ初期化:" + this.initiTime + "ms, モデル変換:"
					+ this.modelTime + "ms, 画像生成:" + this.imageTime + "ms, 合計:"
					+ this.totalTime + "ms"), 5, 35);
		}
	}

	@Override
	public void paint(Graphics g) {
		final long t0 = System.currentTimeMillis();

		// Zバッファ & Image配列の初期化
		for (int i = 0; i < this.pbuf.length; i++) {
			this.pbuf[i] = this.BGCOLOR;
			this.zbuf[i] = -214783647;
		}

		final long t1 = System.currentTimeMillis();

		this.rendering();

		final long t2 = System.currentTimeMillis();

		// IMGの更新を伝える
		this.MIS.newPixels();
		// オフスクリーン => オンスクリーン
		g.drawImage(this.IMG, 0, 0, this);

		final long t3 = System.currentTimeMillis();

		// ベンチマーク
		this.time0 += t1 - t0;
		this.time1 += t2 - t1;
		this.time2 += t3 - t2;
		this.time3 += t3 - t0;

		if (this.count++ == this.loop) {
			this.spf = System.currentTimeMillis() - this.time; // (ms)
			this.time = System.currentTimeMillis();
			this.fps = 1000 * this.loop / (float) this.spf; // (描画回数/s)
			this.initiTime = this.time0 / this.loop;
			this.modelTime = this.time1 / this.loop;
			this.imageTime = this.time2 / this.loop;
			this.totalTime = this.time3 / this.loop;
			this.time0 = 0;
			this.time1 = 0;
			this.time2 = 0;
			this.time3 = 0;
			this.count = 0;
		}
	}

	public void rendering() {
		// 変換行列設定
		Matrix convert = new Matrix();
		convert.initUnit();
		convert.rotateZ(this.angle[2]);
		convert.rotateY(this.angle[0]);
		convert.rotateX(this.angle[1]);
		convert.viewport();

		// 単位ｊベクトル変換用
		Matrix rotation = convert.clone();

		convert.scale(this.scale, this.scale, this.scale);
		convert.translate(this.position);
		convert.fixed();

		// 光源ベクトルの変換
		// rotation.transform(light);
		for (int i = 0; i < this.object.length; i++) {
			if (this.visibleObject[i]) {
				Vertex3D[] vertex = this.object[i].getVertices();
				Face[] face = this.object[i].getFaces();
				// 頂点変換
				for (int j = 0; j < vertex.length; j++) {
					// 頂点の位置ベクトルを変換
					convert.transform(vertex[j]);
				}
				// ラスタライズ
				for (int j = 0; j < face.length; j++) {
					rotation.transform(face[j].getFaceNormal());
					rotation.transform(face[j].getNormal(0));
					rotation.transform(face[j].getNormal(1));
					rotation.transform(face[j].getNormal(2));
					// face[j].getNormal(0).normalize();
					// face[j].getNormal(1).normalize();
					// /face[j].getNormal(2).normalize();
					// 法線ベクトルを変換
					if (face[j].normal()) {
						this.drawFace(face[j]);
					}
				}
			}
		}
		if (this.axisFLAG) {
			Vertex3D origin = new Vertex3D();
			convert.transform(origin);
			Vertex3D[] axis = new Vertex3D[3];
			axis[0] = new Vertex3D(250f, 0f, 0f);
			axis[1] = new Vertex3D(0f, 250f, 0f);
			axis[2] = new Vertex3D(0f, 0f, 250f);
			// draw axis
			for (int i = 0; i < 3; i++) {
				convert.transform(axis[i]);
				this.drawLine(origin, axis[i], this.AXIS_COLOR[i]);
			}
		}
	}

	public void drawFace(Face fc) {

		// 光源ベクトルと面の法線ベクトルの内積
		Vector3D.Float fnormal = fc.getFaceNormal();

		// ここらへんすっきりせーへんかなぁ。
		int[] x = new int[3], y = new int[3], z = new int[3];
		for (int i = 0; i < 3; i++) {
			Vertex3D v = fc.getVertices(i);
			x[i] = v.getTransformedX();
			y[i] = (v.getTransformedY() >> 16);
			// z座標は拡大縮小しないように修正すべき
			z[i] = v.getTransformedZ();
		}
		int y_min, y_max;
		if (y[0] > y[1]) {
			y_min = y[1];
			y_max = y[0];
		} else {
			y_min = y[0];
			y_max = y[1];
		}
		if (y[2] > y_max)
			y_max = y[2];
		else if (y[2] < y_min)
			y_min = y[2];

		// フレーム外の場合
		if (y_min < 0)
			y_min = 0;
		if (y_max > this.CANVAS_HEIGHT)
			y_max = this.CANVAS_HEIGHT;

		// 最大最小バッファの初期化
		for (int i = y_min; i < y_max; i++) {
			this.xMin[i] = this.CANVAS_WIDTH + 1;
			this.xMax[i] = -1;
		}
		// 材質を取得
		Material mat = fc.getMaterial();
		// RGB
		float[] rgb = mat.getColor();
		// dif(拡散光) amb(周囲光 環境光？) emi spc power
		float[] shine = mat.getShine();
		if (fc.smoothRequest()) {
			float[] w = new float[3];
			int[] r = new int[3], g = new int[3], b = new int[3];
			for (int i = 0; i < 3; i++) {
				w[i] = fc.getNormal(i).dotProduct2(this.light);
				// 頂点色を求める
				float tr = rgb[0] * w[i] * shine[0] + shine[1];
				float tg = rgb[1] * w[i] * shine[0] + shine[1];
				float tb = rgb[2] * w[i] * shine[0] + shine[1];
				if (tr < 0)
					tr = 0;
				if (tg < 0)
					tg = 0;
				if (tb < 0)
					tb = 0;
				r[i] = (int) (tr * 0xff0000);
				g[i] = (int) (tg * 0xff0000);
				b[i] = (int) (tb * 0xff0000);
			}
			if (mat.haveTexture()) {
				// UV
				int[] uv = fc.getUV();
				// テクスチャ取得
				int[] tbuf = mat.getTexture();
				int texWidth = mat.getTextureWidth();
				this.scanEdge(x[0], x[1], y[0], y[1], z[0], z[1], r[0], r[1],
						g[0], g[1], b[0], b[1], uv[0], uv[1], uv[3], uv[4]);
				this.scanEdge(x[0], x[2], y[0], y[2], z[0], z[2], r[0], r[2],
						g[0], g[2], b[0], b[2], uv[0], uv[2], uv[3], uv[5]);
				this.scanEdge(x[1], x[2], y[1], y[2], z[1], z[2], r[1], r[2],
						g[1], g[2], b[1], b[2], uv[1], uv[2], uv[4], uv[5]);
				// Zbuffer 法
				// 最大最小バッファに基づいて描画する。
				for (int py = y_min; py < y_max; py++) {
					// 増分値計算
					int l = (this.xMax[py] - this.xMin[py]) + 1;
					int dz = (this.maxZ[py] - this.minZ[py]) / l;
					int dr = (this.maxR[py] - this.minR[py]) / l;
					int dg = (this.maxG[py] - this.minG[py]) / l;
					int db = (this.maxB[py] - this.minB[py]) / l;
					int du = (this.maxU[py] - this.minU[py]) / l;
					int dv = (this.maxV[py] - this.minV[py]) / l;
					// バッファが未更新ならスキップ
					if (this.xMax[py] == -1
							|| this.xMin[py] == this.CANVAS_WIDTH)
						continue;
					int offset = py * this.CANVAS_WIDTH;
					// 初期値設定
					int pz = this.minZ[py];
					int pr = this.minR[py];
					int pg = this.minG[py];
					int pb = this.minB[py];
					int pu = this.minU[py];
					int pv = this.minV[py];
					for (int px = this.xMin[py]; px <= this.xMax[py]; px++, pz += dz, pr += dr, pg += dg, pb += db, pu += du, pv += dv) {
						if (px < 0 || px >= this.CANVAS_WIDTH)
							continue;
						int p = offset + px;
						if (this.zbuf[p] < pz) {
							int sr = pr >> 16;
							int sg = pg >> 16;
							int sb = pb >> 16;
							if (sr > 255)
								sr = 255;
							if (sg > 255)
								sg = 255;
							if (sb > 255)
								sb = 255;
							// テクスチャ位置を取得
							int tp = (pv >> 16) * texWidth + (pu >> 16);
							// テクスチャの内部判定
							if (tp > tbuf.length - 1 || tp < 0)
								this.pbuf[p] = (sr << 16) | (sg << 8) | sb
										| 0xff000000;
							else {
								// テクセルの取得
								int texel = tbuf[tp];
								// テクセルのRGBと頂点色からRGBを乗算する。
								int tr = (sr * ((texel & 0xff0000) >> 16)) >> 8;
								int tg = (sg * ((texel & 0x00ff00) >> 8)) >> 8;
								int tb = (sb * ((texel & 0x0000ff))) >> 8;
								if (tr > 255)
									tr = 255;
								if (tg > 255)
									tg = 255;
								if (tb > 255)
									tb = 255;
								this.pbuf[p] = (tr << 16) | (tg << 8) | tb
										| 0xff000000;
							}
							this.zbuf[p] = pz;
						}
					}
				}
			} else {
				this.scanEdge(x, y, z, r, g, b);
				// ラスタライズ
				for (int py = y_min; py < y_max; py++) {
					// 増分値計算
					int l = (this.xMax[py] - this.xMin[py]) + 1;
					int dz = (this.maxZ[py] - this.minZ[py]) / l;
					int dr = (this.maxR[py] - this.minR[py]) / l;
					int dg = (this.maxG[py] - this.minG[py]) / l;
					int db = (this.maxB[py] - this.minB[py]) / l;
					// バッファが未更新ならスキップ
					if (this.xMax[py] == -1
							|| this.xMin[py] == this.CANVAS_WIDTH)
						continue;
					int offset = py * this.CANVAS_WIDTH;
					// 初期値設定
					int pz = this.minZ[py];
					int pr = this.minR[py];
					int pg = this.minG[py];
					int pb = this.minB[py];
					for (int px = this.xMin[py]; px <= this.xMax[py]; px++, pz += dz, pr += dr, pg += dg, pb += db) {
						if (px < 0 || px >= this.CANVAS_WIDTH)
							continue;
						int p = offset + px;
						if (this.zbuf[p] < pz) {
							this.zbuf[p] = pz;
							int sr = pr >> 16;
							int sg = pg >> 16;
							int sb = pb >> 16;
							if (sr > 255)
								sr = 255;
							if (sg > 255)
								sg = 255;
							if (sb > 255)
								sb = 255;
							this.pbuf[p] = (sr << 16) | (sg << 8) | sb
									| 0xff000000;
						}
					}
				}
			}
		} else {
			float fw = fnormal.dotProduct2(this.light);

			int r = (int) ((rgb[0] * fw * shine[0] + shine[1]) * 255);
			int g = (int) ((rgb[1] * fw * shine[0] + shine[1]) * 255);
			int b = (int) ((rgb[2] * fw * shine[0] + shine[1]) * 255);

			if (r < 0)
				r = 0;
			else if (r > 255)
				r = 255;
			if (g < 0)
				g = 0;
			else if (g > 255)
				g = 255;
			if (b < 0)
				b = 0;
			else if (b > 255)
				b = 255;
			// 面の色
			int color = r << 16 | g << 8 | b | 0xff000000;

			if (mat.haveTexture()) {
				// UV
				int[] uv = fc.getUV();
				// テクスチャ取得
				int[] tbuf = mat.getTexture();
				int texWidth = mat.getTextureWidth();
				this.scanEdge(x[0], x[1], y[0], y[1], z[0], z[1], uv[0], uv[1],
						uv[3], uv[4]);
				this.scanEdge(x[0], x[2], y[0], y[2], z[0], z[2], uv[0], uv[2],
						uv[3], uv[5]);
				this.scanEdge(x[1], x[2], y[1], y[2], z[1], z[2], uv[1], uv[2],
						uv[4], uv[5]);
				// Zbuffer 法
				// 最大最小バッファに基づいて描画する。
				for (int py = y_min; py < y_max; py++) {
					// 増分値計算
					int l = (this.xMax[py] - this.xMin[py]) + 1;
					int dz = (this.maxZ[py] - this.minZ[py]) / l;
					int du = (this.maxU[py] - this.minU[py]) / l;
					int dv = (this.maxV[py] - this.minV[py]) / l;
					// バッファが未更新ならスキップ
					if (this.xMax[py] == -1
							|| this.xMin[py] == this.CANVAS_WIDTH)
						continue;
					int offset = py * this.CANVAS_WIDTH;
					// 初期値設定
					int pz = this.minZ[py];
					int pu = this.minU[py];
					int pv = this.minV[py];
					for (int px = this.xMin[py]; px <= this.xMax[py]; px++, pz += dz, pu += du, pv += dv) {
						if (px < 0 || px >= this.CANVAS_WIDTH)
							continue;
						int p = offset + px;
						if (this.zbuf[p] < pz) {
							// テクスチャ位置を取得
							int tp = (pv >> 16) * texWidth + (pu >> 16);
							// テクスチャの内部判定
							if (tp > tbuf.length - 1 || tp < 0) {
								// 外部の場合
								this.pbuf[p] = color;
								this.zbuf[p] = pz;
							} else {
								// テクセルの取得
								int texel = tbuf[tp];
								// テクセルのRGBと頂点色からRGBを乗算する。
								int tr = (r * ((texel & 0xff0000) >> 16)) >> 8;
								int tg = (g * ((texel & 0x00ff00) >> 8)) >> 8;
								int tb = (b * ((texel & 0x0000ff))) >> 8;
								if (tr > 255)
									tr = 255;
								if (tg > 255)
									tg = 255;
								if (tb > 255)
									tb = 255;
								this.pbuf[p] = (tr << 16) | (tg << 8) | tb
										| 0xff000000;
								this.zbuf[p] = pz;
							}
						}
					}
				}
			} else {
				this.scanEdge(x[0], x[1], y[0], y[1], z[0], z[1]);
				this.scanEdge(x[0], x[2], y[0], y[2], z[0], z[2]);
				this.scanEdge(x[1], x[2], y[1], y[2], z[1], z[2]);
				// ラスタライズ
				for (int py = y_min; py < y_max; py++) {
					// バッファが未更新ならスキップ
					if (this.xMax[py] == -1
							|| this.xMin[py] == this.CANVAS_WIDTH)
						continue;

					// 増分値計算
					int l = (this.xMax[py] - this.xMin[py]) + 1;
					int dz = (this.maxZ[py] - this.minZ[py]) / l;
					int offset = py * this.CANVAS_WIDTH;
					// 初期値設定
					int pz = this.minZ[py];
					for (int px = this.xMin[py]; px <= this.xMax[py]; px++, pz += dz) {
						if (px < 0 || px >= this.CANVAS_WIDTH)
							continue;
						int p = offset + px;
						if (this.zbuf[p] < pz) {
							this.zbuf[p] = pz;
							this.pbuf[p] = color;
						}
					}
				}
			}
		}
	}

	/**
	 * y軸方向に対する直線のエッジ検出(テクスチャ&グローシェーディング対応)
	 * 
	 * @param x0
	 * @param x1
	 * @param y0
	 * @param y1
	 * @param z0
	 * @param z1
	 * @param r0
	 * @param r1
	 * @param g0
	 * @param g1
	 * @param b0
	 * @param b1
	 * @param u0
	 * @param u1
	 * @param v0
	 * @param v1
	 */
	private void scanEdge(int x0, int x1, int y0, int y1, int z0, int z1,
			int r0, int r1, int g0, int g1, int b0, int b1, int u0, int u1,
			int v0, int v1) {
		int l = (y1 - y0);
		l = (l != 0) ? Math.abs(l) : 1;
		// 増分値計算
		int dx = (x1 - x0) / l;
		int dy = (y1 - y0) / l;
		int dz = (z1 - z0) / l;
		int dr = (r1 - r0) / l;
		int dg = (g1 - g0) / l;
		int db = (b1 - b0) / l;
		int du = (u1 - u0) / l;
		int dv = (v1 - v0) / l;
		for (int i = 0; i <= l; i++, x0 += dx, y0 += dy, z0 += dz, r0 += dr, g0 += dg, b0 += db, u0 += du, v0 += dv) {
			int px = x0 >> 16;
			if (y0 < 0 || y0 >= this.CANVAS_HEIGHT)
				continue;
			if (this.xMin[y0] > px) {
				this.xMin[y0] = px;
				this.minZ[y0] = z0;
				this.minR[y0] = r0;
				this.minG[y0] = g0;
				this.minB[y0] = b0;
				this.minU[y0] = u0;
				this.minV[y0] = v0;
			}
			if (this.xMax[y0] < px) {
				this.xMax[y0] = px;
				this.maxZ[y0] = z0;
				this.maxR[y0] = r0;
				this.maxG[y0] = g0;
				this.maxB[y0] = b0;
				this.maxU[y0] = u0;
				this.maxV[y0] = v0;
			}
		}
	}

	/**
	 * y軸方向に対する直線のエッジ検出(テクスチャ対応)
	 * 
	 * @param x0
	 * @param x1
	 * @param y0
	 * @param y1
	 * @param z0
	 * @param z1
	 * @param u0
	 * @param u1
	 * @param v0
	 * @param v1
	 */
	private void scanEdge(int x0, int x1, int y0, int y1, int z0, int z1,
			int u0, int u1, int v0, int v1) {
		int l = (y1 - y0);
		l = (l != 0) ? Math.abs(l) : 1;
		// 増分値計算
		int dx = (x1 - x0) / l;
		int dy = (y1 - y0) / l;
		int dz = (z1 - z0) / l;
		int du = (u1 - u0) / l;
		int dv = (v1 - v0) / l;
		for (int i = 0; i <= l; i++, x0 += dx, y0 += dy, z0 += dz, u0 += du, v0 += dv) {
			int px = x0 >> 16;
			if (y0 < 0 || y0 >= this.CANVAS_HEIGHT)
				continue;
			if (this.xMin[y0] > px) {
				this.xMin[y0] = px;
				this.minZ[y0] = z0;
				this.minU[y0] = u0;
				this.minV[y0] = v0;
			}
			if (this.xMax[y0] < px) {
				this.xMax[y0] = px;
				this.maxZ[y0] = z0;
				this.maxU[y0] = u0;
				this.maxV[y0] = v0;
			}
		}
	}

	/**
	 * y軸方向に対する直線のエッジ検出(グローシェーディング用)
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param r
	 * @param g
	 * @param b
	 */
	private void scanEdge(int[] x, int[] y, int[] z, int[] r, int[] g, int[] b) {
		for (int i = 0; i < 3; i++) {
			// 初期値
			int n = (i + 1) % 3;
			int x0 = x[i];
			int x1 = x[n];
			int y0 = y[i];
			int y1 = y[n];
			int z0 = z[i];
			int z1 = z[n];
			int r0 = r[i];
			int r1 = r[n];
			int g0 = g[i];
			int g1 = g[n];
			int b0 = b[i];
			int b1 = b[n];

			int l = (y1 - y0);
			l = (l != 0) ? Math.abs(l) : 1;
			// 増分値計算
			int dx = (x1 - x0) / l;
			int dy = (y1 - y0) / l;
			int dz = (z1 - z0) / l;
			int dr = (r1 - r0) / l;
			int dg = (g1 - g0) / l;
			int db = (b1 - b0) / l;

			for (int j = 0; j <= l; j++, x0 += dx, y0 += dy, z0 += dz, r0 += dr, g0 += dg, b0 += db) {
				// 固定小数点を整数化
				int px = x0 >> 16;
				if (y0 < 0 || y0 >= this.CANVAS_HEIGHT)
					continue;
				if (this.xMin[y0] > px) {
					this.xMin[y0] = px;
					this.minZ[y0] = z0;
					this.minR[y0] = r0;
					this.minG[y0] = g0;
					this.minB[y0] = b0;
				}
				if (this.xMax[y0] < px) {
					this.xMax[y0] = px;
					this.maxZ[y0] = z0;
					this.maxR[y0] = r0;
					this.maxG[y0] = g0;
					this.maxB[y0] = b0;
				}
			}
		}
	}

	/**
	 * y軸方向に対する直線のエッジ検出
	 * 
	 * @param x0
	 * @param x1
	 * @param y0
	 * @param y1
	 * @param z0
	 * @param z1
	 */
	private void scanEdge(int x0, int x1, int y0, int y1, int z0, int z1) {
		int l = (y1 - y0);
		l = (l != 0) ? Math.abs(l) : 1;
		// 増分値計算
		int dx = (x1 - x0) / l;
		int dy = (y1 - y0) / l;
		int dz = (z1 - z0) / l;
		for (int i = 0; i <= l; i++, x0 += dx, y0 += dy, z0 += dz) {
			// 固定小数点を整数化
			int px = x0 >> 16;
			if (y0 < 0 || y0 >= this.CANVAS_HEIGHT)
				continue;
			if (this.xMin[y0] > px) {
				this.xMin[y0] = px;
				this.minZ[y0] = z0;
			}
			if (this.xMax[y0] < px) {
				this.xMax[y0] = px;
				this.maxZ[y0] = z0;
			}
		}
	}

	/**
	 * 直線描画
	 * 
	 * @param v0
	 * @param v1
	 * @param color
	 */
	public void drawLine(Vertex3D v0, Vertex3D v1, int color) {
		// 増分値計算
		int dx = v1.getTransformedX() - v0.getTransformedX();
		int dy = v1.getTransformedY() - v0.getTransformedY();
		int dz = v1.getTransformedZ() - v0.getTransformedZ();

		int l = (Math.abs(dx) > Math.abs(dy)) ? Math.abs(dx >> 16) + 1 : Math
				.abs(dy >> 16) + 1;

		dx /= l;
		dy /= l;
		dz /= l;
		int x0 = v0.getTransformedX();
		int y0 = v0.getTransformedY();
		int z0 = v0.getTransformedZ();
		for (int i = 0; i < l; i++, x0 += dx, y0 += dy, z0 += dz) {
			// 固定小数点を整数化
			int py = y0 >> 16;
			int px = x0 >> 16;

			if (py < 0 || py >= this.CANVAS_HEIGHT)
				continue;
			if (px < 0 || px >= this.CANVAS_WIDTH)
				continue;

			int p = py * this.CANVAS_WIDTH + px;
			if (this.zbuf[p] < z0) {
				this.zbuf[p] = z0;
				this.pbuf[p] = color;
			}
		}
	}

	/**
	 * Object3Dの可視設定
	 * 
	 * @param index
	 *            設定するオブジェクトインデックス
	 * @param st
	 *            更新するステータス
	 */
	public void setVisibleObject(int index, boolean st) {
		assert index > this.object.length;
		this.visibleObject[index] = st;

		this.reload();
	}

	/**
	 * 回転角制御設定
	 * 
	 * @param n
	 * @param minagl
	 *            最小角
	 * @param maxagl
	 *            最大角
	 */
	public void control(int n, float minagl, float maxagl) {
		this.ANGLE_CONTROL[n] = true;
		this.ANGLE_MAX[n] = maxagl;
		this.ANGLE_MIN[n] = minagl;
	}

	/**
	 * 倍率設定
	 * 
	 * @param d
	 */
	public void setScale(int d) {
		this.scale *= (1 + d * this.SENSE[0]);
		if (this.scale < this.SCALE_MIN)
			this.scale = this.SCALE_MIN;
		if (this.scale > this.SCALE_MAX)
			this.scale = this.SCALE_MAX;
		this.reload();
	}

	/**
	 * 角度設定
	 * 
	 * @param da
	 */
	public void setAngle(int[] da) {
		for (int i = 0; i < 3; i++) {
			this.angle[i] += da[i] * this.SENSE[1];
			if (this.ANGLE_CONTROL[i])
				if (this.angle[i] < this.ANGLE_MIN[i])
					this.angle[i] = this.ANGLE_MIN[i];
				else if (this.angle[i] > this.ANGLE_MAX[i])
					this.angle[i] = this.ANGLE_MAX[i];
		}
		this.reload();
	}

	/**
	 * 位置設定
	 * 
	 * @param d
	 */
	public void setPosition(int[] d) {
		this.position[0] += d[0];
		this.position[1] += d[1];
		this.position[2] += d[2];
		this.reload();
	}
}