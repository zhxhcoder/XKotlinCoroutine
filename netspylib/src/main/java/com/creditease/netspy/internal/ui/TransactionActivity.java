
package com.creditease.netspy.internal.ui;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.creditease.netspy.R;
import com.creditease.netspy.internal.data.NetSpyContentProvider;
import com.creditease.netspy.internal.data.HttpTransaction;
import com.creditease.netspy.internal.data.LocalCupboard;
import com.creditease.netspy.internal.support.FormatUtils;
import com.creditease.netspy.internal.support.SimpleOnPageChangedListener;

import java.util.ArrayList;
import java.util.List;

import static com.creditease.netspy.internal.ui.TransactionPayloadFragment.TYPE_REQUEST;
import static com.creditease.netspy.internal.ui.TransactionPayloadFragment.TYPE_RESPONSE;

public class TransactionActivity extends BaseNetSpyActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String ARG_TRANSACTION_ID = "transaction_id";

    private static int selectedTabPosition = 0;

    public static void start(Context context, long transactionId) {
        Intent intent = new Intent(context, TransactionActivity.class);
        intent.putExtra(ARG_TRANSACTION_ID, transactionId);
        context.startActivity(intent);
    }

    TextView title;
    Adapter adapter;

    private long transactionId;
    private HttpTransaction transaction;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.netspy_activity_transaction);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        title = findViewById(R.id.toolbar_title);

        final ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        ViewPager viewPager = findViewById(R.id.viewpager);
        if (viewPager != null) {
            setupViewPager(viewPager);
        }

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        transactionId = getIntent().getLongExtra(ARG_TRANSACTION_ID, 0);
        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.netspy_transaction, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.share_text) {
            share(FormatUtils.getShareText(this, transaction));
            return true;
        } else if (item.getItemId() == R.id.share_curl) {
            share(FormatUtils.getShareCurlCommand(transaction));
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = new CursorLoader(this);
        loader.setUri(ContentUris.withAppendedId(NetSpyContentProvider.TRANSACTION_URI, transactionId));
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        transaction = LocalCupboard.getInstance().withCursor(data).get(HttpTransaction.class);
        populateUI();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    private void populateUI() {
        if (transaction != null) {
            title.setText(transaction.getMethod() + " " + transaction.getPath());
            for (TransactionFragment fragment : adapter.fragments) {
                fragment.transactionUpdated(transaction);
            }
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        adapter = new Adapter(getSupportFragmentManager());
        adapter.addFragment(new TransactionOverviewFragment(), getString(R.string.netspy_overview));
        adapter.addFragment(TransactionPayloadFragment.newInstance(TYPE_REQUEST), getString(R.string.netspy_request));
        adapter.addFragment(TransactionPayloadFragment.newInstance(TYPE_RESPONSE), getString(R.string.netspy_response));
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new SimpleOnPageChangedListener() {
            @Override
            public void onPageSelected(int position) {
                selectedTabPosition = position;
            }
        });
        viewPager.setCurrentItem(selectedTabPosition);
    }

    private void share(String content) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, content);
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, null));
    }

    static class Adapter extends FragmentPagerAdapter {
        final List<TransactionFragment> fragments = new ArrayList<>();
        private final List<String> fragmentTitles = new ArrayList<>();

        Adapter(FragmentManager fm) {
            super(fm);
        }

        void addFragment(TransactionFragment fragment, String title) {
            fragments.add(fragment);
            fragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return (Fragment) fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitles.get(position);
        }
    }
}
