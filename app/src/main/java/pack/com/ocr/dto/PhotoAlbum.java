package pack.com.ocr.dto;

import android.net.Uri;

/**
 * Created by kinamare on 2017-09-06.
 */

public class PhotoAlbum {


	public PhotoAlbum(Uri thumnailUri, Uri imageUri){
		this.thumnailUri = thumnailUri;
		this.imageUri = imageUri;
	}

	public Uri getThumnailUri() {
		return thumnailUri;
	}

	public Uri getImageUri() {
		return imageUri;
	}

	Uri thumnailUri;
	Uri imageUri;

}
