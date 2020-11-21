package renderer;
import java.awt.Label;
import java.awt.Checkbox;
import java.awt.BorderLayout;

import javax.swing.JFrame;

/**
 * Java Mqo Renderer
 * --- 必須設定のパラメータ ---
 * model : モデルファイル名 (対応ファイル *.mqo)
 * 
 * --- 任意設定のパラメータ ---
 * bgcolor : 背景色 (6桁の16進数で記述)
 * object  : モデルファイルから読み込むオブジェクト数の上限
 * bench   : benchmark の設定
 * texture : テクスチャの設定
 * scale   : 倍率
 * smooth  : グローシェーディングを行う接面の最大角度
 * 
 * 05/06/08 ver.0.03 Shiftによる回転制御
 *          ver.0.04 オフスクリーン採用
 *                   モデル平行移動
 * 05/06/09 ver.0.05 モデルの回転を最適化
 *          ver.0.06 陰面処理　：　Zソート法 => 法線ベクトル法
 * 05/06/10 ver.0.07 辺 <=> 面　表示の切り替え
 *          ver.0.08 Flat class から ObjPanel class　を生成
 *                   Object3D class の実装
 *          ver.0.09 陰面処理 : 法線ベクトル法　=> Zソート法
 *                   ObjPanel を ModelPanel にリネーム
 *                   Flat をMrender にリネーム
 * 05/06/11 ver.0.10 ホイールによるモデルの拡大縮小
 *                   ビューイング変換実装
 *          ver.0.11 簡易なフラットシェーディング実装
 *          ver.0.12 mqoファイル読み込み実装
 *                   ビューイング変換を最適化
 * 05/06/12 ver.0.13 モデルファイルが見つからなかったときに強制終了させる
 *                   Zバッファ法による陰面除去の実装
 *          ver.0.14 Zバッファ法によるバグ修正
 * 05/06/14 ver.0.15 エッジ検出の最適化
 *          ver.0.16 mqoファイルから複数オブジェクトの読込表示を実装
 *          ver.0.17 最適化により高速化&マウス追従の最適化
 * 05/06/21 ver.0.18 Javaアプレット化
 *                   オブジェクト名の表示
 *          ver.0.19 アプレットに最適化
 *                   モデルのＨＴＭＬからの指定を実現
 *          ver.0.20 オブジェクトの表示の選択を実現
 * 05/06/22 ver.0.21 材質名の表示
 *                   面(Face)に色の設定を実現
 *          ver.0.22 とりあえず辺表示実装
 *                   面、辺の表示切替簡易実装
 * 05/07/04 ver.0.23 行列による座標変換
 * 05/07/05 ver.0.24 三角ポリゴンによる描画
 *          ver.0.25 Vertexクラスの最適化(Vectorを拡張)
 *          ver.0.26 paintメソッド更新
 * 05/07/07 ver.0.27 マイナーチェンジ(とりあえずback up)
 * 05/07/08 ver.0.28 Renderクラス実装、そして最適化(かなりの高速化)
 *                   MemoryImageSourceによるフレームバッファ
 *                    - CANVASサイズ依存の負荷激減
 *                    - 画像生成処理の基本負荷は増加
 *                    - 480×360 以上推奨
 *                    - 描画が実際よりワンステップ遅れて表示されている。
 *          ver.0.29 Renderをインスタンスで利用
 *                   背景色設定
 * 05/07/09 ver.0.30 モデルデータ構成、変換を参照渡しを用い最適化(モデル変換高速化)
 * 05/07/10 ver.0.31 オブジェクトの可視設定を復旧
 *                   newPixels()によって生じるコマ遅れを修正
 * 05/07/11 ver.0.32 材質ごとの環境(周囲光)、拡散光の指定
 *                   テクスチャマッピングの実装
 * 05/07/15 ver.0.34 VectorクラスをVector3Dクラスに更新
 * 05/07/17 ver.0.35 scanEdgeの修正(それでも面間に無駄な空間は多少残るとおもわれる)
 * 05/07/19 ver.0.36 グローシェーディングの実装
 * 
 * 思案事項
 *  - ラスタライズは各マテリアルごとに行ったほうが後々早くなりそう。
 *  - 独自フォーマットのモデルファイルを作成すべきかと。
 *  - MemoryImageSource を継承してアニメーションしないように修正すべきかも
 * 
 * @author ma38su
 */
public class Main {
	
	// 回転量
	private static final float[] SENSE = {.1f, .01f};

	public static void main(String[] args) {
		
		JFrame frame = new JFrame("MQO RENDERER");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(600, 400);
		
		// --- Parameter Read ---
		boolean[] flags = new boolean[2];
		flags[0] = false;
		
		// object Read (mqoのから読み込むオブジェクトの上限)
		int object_length = 30;
		Face.setSmoothing((float)Math.cos(30 * Math.PI / 180));

		MqoReader mqo = new MqoReader();
		if (!mqo.objRead(frame, System.class.getResourceAsStream("/.model/umbrella.mqo"), object_length)) {
			System.out.println("mqo file cannot read.");
			System.exit(1);
		}
		
		float scale = 1f;
		
		// get File Data
		Object3D[] obj = mqo.getObject3D();
		Material[] mat = mqo.getMaterial();

		int bgcolor = 0xff000000;
		
		ModelCanvas canvas = new ModelCanvas(obj, mat, frame.getWidth(), frame.getHeight(), scale, bgcolor, flags, SENSE);
		// set Device Listeners
		Controller mouse  = new Controller(canvas);
		frame.addMouseListener(mouse);
		frame.addMouseMotionListener(mouse);
		frame.addMouseWheelListener(mouse);
		frame.addKeyListener(mouse);
		
		// モデル管理パネルの作成
		ObjPanel panel = new ObjPanel();
		
		// モデル表示のためのチェックボックス
		panel.add(new Label("---------------"));
		Checkbox[] objCbox = new Checkbox[obj.length];
		for (int i = 0; i < obj.length; i++) {
			objCbox[i] = new Checkbox(obj[i].name, true);
			objCbox[i].addItemListener(new ObjCboxSet(canvas, i));
			panel.add(objCbox[i]);
		}
		panel.add(new Label("---------------"));
		// 材質をラベルで表示
		Label[] matLabel= new Label[mat.length];
		for (int i = 0; i < mat.length; i++) {
			matLabel[i] = new Label(mat[i].getName());
			panel.add(matLabel[i]);
		}
		frame.add(canvas, BorderLayout.CENTER);
		frame.setVisible(true);
	}
}