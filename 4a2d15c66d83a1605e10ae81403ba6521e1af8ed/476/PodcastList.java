package com.jadn.cc.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.jadn.cc.R;
import com.jadn.cc.core.CarCastApplication;
import com.jadn.cc.core.Util;
import com.jadn.cc.services.ContentService;
import com.jadn.cc.services.DownloadHistory;
import com.jadn.cc.services.MetaFile;
import com.jadn.cc.services.MetaHolder;

public class PodcastList extends BaseActivity {

	SimpleAdapter podcastsAdapter;
	ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			showPodcasts();
		}
	}

	@Override
	protected void onContentService() {
		showPodcasts();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.podcast_list_wbar);

		setTitle(CarCastApplication.getAppTitle() + ": Downloaded podcasts");

		Button deleteButton = (Button) findViewById(R.id.delete);
		deleteButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(PodcastList.this).setIcon(android.R.drawable.ic_dialog_alert)
						.setMessage("Delete " + checkedItems.size() + " podcasts?")
						.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								if (contentService.isPlaying())
									contentService.pauseNow();
								while (!checkedItems.isEmpty()) {
									contentService.deletePodcast(checkedItems.last());
									checkedItems.remove(checkedItems.last());
								}
								podcastsAdapter.notifyDataSetChanged();
								showPodcasts();
							}
						}).setNegativeButton("Cancel", null).show();
			}
		});

		((Button) findViewById(R.id.top)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (contentService.isPlaying())
					contentService.pauseNow();
				checkedItems = contentService.moveTop(checkedItems);
				podcastsAdapter.notifyDataSetChanged();
				showPodcasts();
			}
		});

		((Button) findViewById(R.id.up)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (contentService.isPlaying())
					contentService.pauseNow();
				checkedItems = contentService.moveUp(checkedItems);
				podcastsAdapter.notifyDataSetChanged();
				showPodcasts();
			}
		});

		((Button) findViewById(R.id.down)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (contentService.isPlaying())
					contentService.pauseNow();
				checkedItems = contentService.moveDown(checkedItems);
				podcastsAdapter.notifyDataSetChanged();
				showPodcasts();
			}
		});

		((Button) findViewById(R.id.bottom)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (contentService.isPlaying())
					contentService.pauseNow();
				checkedItems = contentService.moveBottom(checkedItems);
				podcastsAdapter.notifyDataSetChanged();
				showPodcasts();
			}
		});
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add("Play");
		menu.add("Delete");
		menu.add("Delete All Before");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.podcasts_menu, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {

		if (item.getItemId() == R.id.deleteListenedTo) {
			String currTitle = "";
			currTitle = contentService.currentTitle();
			MetaHolder metaHolder = new MetaHolder(getApplicationContext());
			for (int i = metaHolder.getSize() - 1; i >= 0; i--) {
				MetaFile metaFile = metaHolder.get(i);
				if (currTitle.equals(metaFile.getTitle())) {
					continue;
				}
				if (metaFile.getDuration() <= 0) {
					continue;
				}
				if (metaFile.isListenedTo()) {
					contentService.deletePodcast(i);
					list.remove(i);
				}
			}
			podcastsAdapter.notifyDataSetChanged();

		} else if (item.getItemId() == R.id.deleteAllPodcasts) {

			// Ask the user if they want to really delete all
			new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Delete All?")
					.setMessage("Do you really want to delete all downloaded podcasts?")
					.setPositiveButton("Confirm Delete All", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							contentService.purgeAll();
							list.clear();
							podcastsAdapter.notifyDataSetChanged();
							finish();
						}

					}).setNegativeButton("Cancel", null).show();

			return true;
		}
		if (item.getItemId() == R.id.eraseDownloadHistory) {
			new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setMessage("Erase Download History?")
					.setPositiveButton("Erase", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							int historyDeleted = new DownloadHistory(getApplicationContext()).eraseHistory();
							Util.toast(PodcastList.this, "Erased " + historyDeleted + " podcast from dowload history.");
						}

					}).setNegativeButton("Cancel", null).show();

		}
		return super.onMenuItemSelected(featureId, item);
	}

	protected void showPodcasts() {

		ListView listView = (ListView) findViewById(R.id.list);

		MetaHolder metaHolder = new MetaHolder(getApplicationContext());
		list.clear();

		for (int i = 0; i < metaHolder.getSize(); i++) {
			MetaFile metaFile = metaHolder.get(i);
			HashMap<String, String> item = new HashMap<String, String>();
			if (contentService.currentTitle().equals(metaFile.getTitle())) {
				if (contentService.isPlaying()) {
					item.put("line1", "> " + metaFile.getFeedName());
				} else {
					item.put("line1", "|| " + metaFile.getFeedName());
				}
			} else {
				item.put("line1", metaFile.getFeedName());
			}
			String time = ContentService.getTimeString(metaFile.getCurrentPos()) + "-"
					+ ContentService.getTimeString(metaFile.getDuration());
			if (metaFile.getCurrentPos() == 0 && metaFile.getDuration() == -1) {
				time = "";
			}
			if (metaFile.isListenedTo()) {
				item.put("listened", "true");
				time = "End" + "-" + ContentService.getTimeString(metaFile.getDuration());
			}
			item.put("description", metaFile.getDescription());
			item.put("xx:xx-xx:xx", time);
			item.put("line2", metaFile.getTitle());
			list.add(item);

		}

		// When doing a delete before, we rebuild the list, but the adapter is ok.
		if (podcastsAdapter == null) {
			// TODO this needs to be a full-fledged custom adapter, for better performance
			// and simplicity.
			podcastsAdapter = new SimpleAdapter(this, list, R.layout.podcast_items_checks,
					new String[] { "line1", "xx:xx-xx:xx", "line2", "description" },
					new int[] { R.id.firstLine, R.id.amountHeard, R.id.secondLine, R.id.description }) {
				@Override
				public View getView(int position, View convertView, ViewGroup parent) {
					View view = super.getView(position, convertView, parent);
					@SuppressWarnings("unchecked")
					Map<String, String> map = (Map<String, String>) getItem(position);
					if (map.get("listened") != null) {
						view.setBackgroundColor(Color.rgb(0, 70, 70));
					} else {
						view.setBackgroundColor(Color.TRANSPARENT);
					}
					final CheckBox checkbox = (CheckBox) view.findViewById(R.id.checkBox1);
					checkbox.setOnClickListener(checkBoxClicked);
					view.setOnClickListener(itemClicked);
					view.setOnLongClickListener(itemLongClicked);

					Tag tag = (Tag) view.getTag();
					if (tag == null) {
						view.setTag(tag = new Tag());
					}
					tag.position = position;
					tag.item = map;

					checkbox.setChecked(checkedItems.contains(position));

					// See if we should try to strip out HTML from the description.
					String desc = map.get("description");
					if (desc != null) {
						TextView description = (TextView) view.findViewById(R.id.description);
						// Strip the HTML and then go back to a string to drop formatting.
						description.setText(Html.fromHtml(desc).toString());
					}
					return view;
				}
			};

			listView.setAdapter(podcastsAdapter);
		} else {
			podcastsAdapter.notifyDataSetChanged();
		}

	}

	SortedSet<Integer> checkedItems = new TreeSet<Integer>();

	class Tag {
		int position;
		Map item;
	};

	OnClickListener checkBoxClicked = new OnClickListener() {
		public void onClick(View v) {
			final CheckBox checkbox = (CheckBox) v;
			View pView = (View) v.getParent();
			Tag tag = (Tag) pView.getTag();
			if (checkbox.isChecked()) {
				checkedItems.add(tag.position);
			} else {
				checkedItems.remove(tag.position);
			}
			// v.getTag()
			for (Button button : getBarButtons()) {
				button.setEnabled(!checkedItems.isEmpty());
			}
		}

	};

	public List<Button> getBarButtons() {
		List<Button> barButtons = new ArrayList<Button>();
		barButtons.add((Button) findViewById(R.id.delete));
		barButtons.add((Button) findViewById(R.id.top));
		barButtons.add((Button) findViewById(R.id.up));
		barButtons.add((Button) findViewById(R.id.down));
		barButtons.add((Button) findViewById(R.id.bottom));
		return barButtons;
	}

	OnClickListener itemClicked = new OnClickListener() {
		public void onClick(View v) {
			Tag tag = (Tag) v.getTag();

			MetaHolder metaHolder = new MetaHolder(getApplicationContext());
			MetaFile mfile = metaHolder.get(tag.position);

			if (mfile.getTitle().equals(contentService.currentTitle())) {
				contentService.pauseOrPlay();
			} else {
				// This saves our position
				if (contentService.isPlaying())
					contentService.pauseNow();
				contentService.play(tag.position);
			}
			showPodcasts();
		}
	};

	OnLongClickListener itemLongClicked = new OnLongClickListener() {
		public boolean onLongClick(View v) {
			final Tag tag = (Tag) v.getTag();

			final MetaHolder metaHolder = new MetaHolder(getApplicationContext());
			final MetaFile mfile = metaHolder.get(tag.position);

			// Ask the user if they want to really delete all
			new AlertDialog.Builder(PodcastList.this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Delete Before?")
					.setMessage("Delete all before " + mfile.getTitle())
					.setPositiveButton("Confirm Delete " + tag.position + " podcasts", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (contentService.isPlaying())
								contentService.pauseNow();

							while ((tag.position--) != 0) {
								contentService.deletePodcast(0);
							}

							podcastsAdapter.notifyDataSetChanged();
                            showPodcasts();
						}

					}).setNegativeButton("Cancel", null).show();

			return true;
		}
	};

}
