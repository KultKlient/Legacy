package kult.klient.gui.screens.music;

import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import kult.klient.gui.tabs.builtin.MusicTab;
import kult.klient.gui.widgets.containers.WTable;
import kult.klient.gui.widgets.input.WTextBox;
import kult.klient.gui.widgets.music.WMusicWidget;
import kult.klient.gui.widgets.music.WPaginationProvider;
import kult.klient.gui.GuiTheme;
import kult.klient.gui.WindowScreen;
import kult.klient.utils.music.PlaylistUtils;
import kult.klient.utils.music.SearchUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static kult.klient.KultKlient.mc;

public class PlaylistsScreen extends WindowScreen {
    private final MusicTab.MusicScreen musicScreen;
    private List<WMusicWidget> childWidgets;
    private WTable table;
    private WPaginationProvider pagination;

    public PlaylistsScreen(GuiTheme theme, MusicTab.MusicScreen musicScreen) {
        super(theme, "Music - Playlists");
        this.musicScreen = musicScreen;
        this.parent = musicScreen;
    }

    @Override
    public void initWidgets() {
        super.init();
        childWidgets = new ArrayList<>();

        pagination = new WPaginationProvider(j -> construct());
        childWidgets.add(pagination);

        if (table != null) table.clear();
        clear();
        table = add(theme.table()).expandX().minWidth(300).widget();
        construct();
    }

    public void construct() {
        pagination.setMaxPage(PlaylistUtils.count() / MusicTab.MusicScreen.pageSize);
        table.clear();

        WTextBox box = table.add(theme.textBox("")).expandX().widget();
        table.add(theme.plus()).widget().action = () -> SearchUtils.search(box.get(), playlist -> {
                PlaylistUtils.add(box.get());
                construct();
            });

        table.row();
        table.add(theme.button("Reset")).expandX().widget().action = () -> {
            PlaylistUtils.reset();
            construct();
        };

        List<Map.Entry<String, AudioPlaylist>> keys = PlaylistUtils.getEntriesOrdered();
        for (int i = 0; i < keys.size(); i++) {
            int j = i + pagination.getPageOffset();
            table.row();
            AudioPlaylist playlist = keys.get(j).getValue();

            table.add(theme.button(playlist.getName())).expandX().widget().action = () -> mc.setScreen(new PlaylistViewScreen(theme, playlist, musicScreen).setParent(this));

            table.add(theme.minus()).right().widget().action = () -> {
                PlaylistUtils.remove(keys.get(j).getKey());
                construct();
            };
        }

        for (WMusicWidget wMusicWidget : childWidgets) {
            wMusicWidget.add(table, musicScreen, theme);
        }
    }
}
