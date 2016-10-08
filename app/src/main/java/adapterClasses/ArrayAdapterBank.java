package adapterClasses;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.atm.atmlocator.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import modelClasses.BankModel;

/**
 * Created by Tahmidul on 6/20/2016.
 */

public class ArrayAdapterBank extends ArrayAdapter<BankModel> {

    private final Activity context;
    private static List<BankModel> list;
    private int positionPic;
    private ImageView holderImage;
    private int i = 0;

    public ArrayAdapterBank(Activity context, List<BankModel> list) {
        super(context, R.layout.listitem_offline_layout, list);
        this.context = context;
        this.list = list;
    }

    class ViewHolder {
        protected TextView text1, text2, text3;
        //protected ImageView image1;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = null;
        if (convertView == null) {

            LayoutInflater inflator = context.getLayoutInflater();
            view = inflator.inflate(R.layout.listitem_offline_layout, null);
            final ViewHolder viewHolder = new ViewHolder();
            viewHolder.text1 = (TextView) view.findViewById(R.id.txtv_atmName);
            viewHolder.text2 = (TextView) view.findViewById(R.id.txtv_bankName);
            viewHolder.text3 = (TextView) view.findViewById(R.id.txtv_addrsBank);
            //viewHolder.image1 = (ImageView) view.findViewById(R.id.imageView_photoPlayer);
            view.setTag(viewHolder);

        } else {
            view = convertView;
        }
        positionPic = position;
        final ViewHolder holder = (ViewHolder) view.getTag();
        holder.text1.setText(Html.fromHtml(list.get(position).getBatmname()));
        holder.text2.setText(Html.fromHtml(list.get(position).getBname()));
        holder.text3.setText(Html.fromHtml(list.get(position).getBaddress()));

        //holder.image1.setId(position);
        //holder.image1.setImageBitmap(list.get(position).getPlbitmap());

        /*if(list.get(position).getPsbitmap()!=null) {
            holder.image1.setImageBitmap(list.get(position).getPsbitmap());
        }
        else {
            //new PhotoStoryActivity().startPicDownload(list.get(position).getPsfeatured_media(), holder.image1, position);
            //i++;
            //photoStoryD[i] = new PhotoStoryPicDownloadAsync(list.get(position).getPsfeatured_media(), holder.image1, position);
                    //photoStoryD[i].executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            new PhotoStoryPicDownloadAsync(list.get(position).getPsfeatured_media(), holder.image1, position).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }*/
        //view.setOnClickListener(new OnItemClickListener(position, list.get(position).getPltitle(), list.get(position).getPlid()));
        return view;
    }
}

    /*private class OnItemClickListener  implements View.OnClickListener {
        private int mPosition;
        private String playerName, playerId;


        OnItemClickListener(int position, String name, String playerId){
            mPosition = position;
            this.playerName = name;
            this.playerId = playerId;
        }

        @Override
        public void onClick(View arg0) {
            Intent intent = new Intent(context, PlayerDtlActivity.class);
            intent.putExtra("picPos", mPosition);
            intent.putExtra("playerName", playerName);
            intent.putExtra("playerId", playerId);
            //intent.setType("text/plain");
            //intent.putExtra(Intent.EXTRA_TEXT, list.get(mPosition).getQlink());
            //context.startActivity(intent);
            context.startActivityForResult(intent, 1);
        }
    }

    private class PhotoStoryPicDownloadAsync extends AsyncTask<ImageView, Void, Bitmap> {

        private JSONObject jobject, jobject2, jobject3;
        private JSONArray jarray;
        private String qid, qlink, qcontent, qtitle, psfeatured_mdeia;
        private int i = 0, picPos;
        private ProgressBar pb;
        private ImageView imageView;
        private URL photostoryPicUrl;
        private String photoStoryPicPrerurl;
        private Bitmap photoStorybitmap;


        public PhotoStoryPicDownloadAsync(String url, ImageView imageView, int picPos) {
            this.psfeatured_mdeia = url;
            this.imageView = imageView;
            this.picPos = picPos;
        }

        protected Bitmap doInBackground(ImageView... imageViews) {

            String url = psfeatured_mdeia;
            String result = null;
            result = new HttpDataHandler().GetHTTPData(url);
            if(result != null){
                try {
                    jobject = new JSONObject(result);
                    photoStoryPicPrerurl = jobject.getJSONObject("media_details").getJSONObject("sizes").getJSONObject("thumbnail").getString("source_url");
                    photostoryPicUrl = new URL(photoStoryPicPrerurl);
                    photoStorybitmap = BitmapFactory.decodeStream(photostoryPicUrl.openConnection().getInputStream());
                    list.get(picPos).setPlbitmap(photoStorybitmap);
                    //new PhotoStoryActivity().getPhotoStoryList().get(picPos).setPsbitmap(photoStorybitmap);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {

            super.onPostExecute(result);
            //System.out.println("Downloaded " + imageView.getId());
            //list.get(positionPic).setQuoter_bitmap(quoterbitmap);
            //new PhotoStoryActivity().notifyPhotoStoryAdapter();
            imageView.setImageBitmap(photoStorybitmap);
        }
    }

/*
    private class OnItemClickListener  implements View.OnClickListener {
        private int mPosition;

        OnItemClickListener(int position){
            mPosition = position;
        }

        @Override
        public void onClick(View arg0) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, list.get(mPosition).getQlink());
            context.startActivity(Intent.createChooser(intent, "Share With"));
        }
    }*/

