package com.rj.newsgateway;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class ArticleFragment extends Fragment {
    private static final String TAG = "ArticleFragment";
    TextView headline;
    TextView date;
    TextView author;
    TextView content;
    ImageView photo;
    TextView count;
    Article article;
    int count1;
    View v;

    public static final String ARTICLE = "ARTICLE";
    public static final String INDEX = "INDEX";
    public static final String TOTAL = "TOTAL";

    public static final ArticleFragment newInstance(Article article, int index, int total) {
        Log.d(TAG, "newInstance: ");
        ArticleFragment fragment = new ArticleFragment();
        Bundle bdl = new Bundle(1);
        bdl.putSerializable(ARTICLE, article);
        bdl.putInt(INDEX, index);
        bdl.putInt(TOTAL, total);
        fragment.setArguments(bdl);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        article = (Article) (getArguments() != null ? getArguments().getSerializable(ARTICLE) : null);
        count1 = getArguments().getInt(INDEX) + 1;
        int total = getArguments().getInt(TOTAL);
        String lastLine = count1 + " of " + total;

        v = inflater.inflate(R.layout.fragment_article, container, false);
        headline = v.findViewById(R.id.headline);
        date = v.findViewById(R.id.date);
        author = v.findViewById(R.id.author);
        content = v.findViewById(R.id.content);
        count = v.findViewById(R.id.index);
        photo = v.findViewById(R.id.photo);

        count.setText(lastLine);
        if (article.getTitle() != null) {
            headline.setText(article.getTitle());
        } else {
            headline.setText("");
        }

        if (article.getPublishingDate() != null && !article.getPublishingDate().isEmpty()) {
            String stringDate = article.getPublishingDate();
            Date date = null;
            String public_date;
            try {
                if (stringDate != null) {
                    date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(stringDate);
                }
                String pattern = "MMM dd, yyyy HH:mm";
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
                public_date = simpleDateFormat.format(date);
                this.date.setText(public_date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (article.getDescription() != null) {
            content.setText(article.getDescription());
        } else {
            content.setText("");
        }
        if (article.getAuthor() != null) {
            author.setText(article.getAuthor());
        } else {
            author.setText("Unknown");
        }
        author.setMovementMethod(new ScrollingMovementMethod());
        if (article.getUrlToImage() != null) {
            loadRemoteImage(article.getUrlToImage());
        }

        headline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToNews();
            }
        });
        content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToNews();
            }
        });
        author.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToNews();
            }
        });
        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToNews();
            }
        });
        return v;
    }

    private void goToNews() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setData(Uri.parse(article.getArticleUrl()));
        startActivity(intent);
    }


    private void loadRemoteImage(final String imageURL) {

        if (imageURL != null) {
            Picasso picasso = new Picasso.Builder(getActivity()).listener(new Picasso.Listener() {
                @Override
                public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                    final String changedUrl = imageURL.replace("http:", "https:");
                    picasso.load(changedUrl)
                            .fit()
                            .centerCrop()
                            .error(R.drawable.brokenimage)
                            .placeholder(R.drawable.placeholder)
                            .into(photo);
                }
            }).build();
            picasso.load(imageURL)
                    .fit()
                    .centerCrop()
                    .error(R.drawable.brokenimage)
                    .placeholder(R.drawable.placeholder)
                    .into(photo);
        } else {
            Picasso.with(getActivity()).load(imageURL)
                    .fit()
                    .centerCrop()
                    .error(R.drawable.brokenimage)
                    .placeholder(R.drawable.missingimage)
                    .into(photo);
        }
    }
}
