package renderer;
import java.awt.Image;
import java.awt.image.PixelGrabber;

public class Material {
	private float[] color = new float[3];
	private String name;
	private int[] pbuf;
	private float[] shine = new float[4];
	private boolean textureFLAG;
	private int textWidth, textHeight;
	public Material () {
	}
	public Material (String name, float[] color, float[] shine, Image img) {
		this.name = name;
		this.color = color;
		this.shine = shine;
		if (img != null) {
			this.textureFLAG = true;
			this.textWidth  = img.getWidth (null);
			this.textHeight = img.getHeight(null);
			this.pbuf = new int[this.textWidth * this.textHeight];
			PixelGrabber pg = new PixelGrabber(img, 0, 0, this.textWidth, this.textHeight, this.pbuf, 0, this.textWidth);
			try{
				pg.grabPixels();
			}catch(InterruptedException e){
				System.out.println(name + ":"+ e);
			}
		}
	}
	public float[] getColor () {
		return this.color;
	}
	public String getName () {
		return this.name;
	}
	public float[] getShine () {
		return this.shine;
	}
	public int[] getTexture() {
		return this.pbuf;
	}
	public int getTextureHeight() {
		return this.textHeight;
	}
	public int getTextureWidth() {
		return this.textWidth;
	}
	// テクスチャを持つか否か
	public boolean haveTexture() {
		return this.textureFLAG;
	}
	@Override
	public String toString () {
		return (this.name + ":" + this.color[0] + "," + this.color[1] + ","+ this.color[2]);
	}
}