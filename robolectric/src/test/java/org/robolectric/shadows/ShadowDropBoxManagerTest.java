package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.os.DropBoxManager;
import android.os.DropBoxManager.Entry;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

/** Unit tests for {@see ShadowDropboxManager}. */
@RunWith(RobolectricTestRunner.class)
public class ShadowDropBoxManagerTest {

  private static final String TAG = "TAG";
  private static final String ANOTHER_TAG = "ANOTHER_TAG";
  private static final byte[] DATA = "HELLO WORLD".getBytes();

  private DropBoxManager manager;
  private ShadowDropBoxManager shadowDropBoxManager;

  @Before
  public void setup() {
    manager =
        (DropBoxManager) RuntimeEnvironment.application.getSystemService(Context.DROPBOX_SERVICE);
    shadowDropBoxManager = shadowOf(manager);
  }

  @Test
  public void emptyDropbox() {
    assertThat(manager.getNextEntry(null, 0)).isNull();
  }

  @Test
  public void dataExpected() throws Exception {
    shadowDropBoxManager.addData(TAG, 1, DATA);

    Entry entry = manager.getNextEntry(null, 0);
    assertThat(entry).isNotNull();
    assertThat(entry.getTag()).isEqualTo(TAG);
    assertThat(entry.getTimeMillis()).isEqualTo(1);
    assertThat(new BufferedReader(new InputStreamReader(entry.getInputStream())).readLine())
        .isEqualTo(new String(DATA));
    assertThat(entry.getText(100)).isEqualTo(new String(DATA));
  }

  /** Checks that we retrieve the first entry <em>after</em> the specified time. */
  @Test
  public void dataNotExpected_timestampSameAsEntry() throws Exception {
    shadowDropBoxManager.addData(TAG, 1, DATA);

    assertThat(manager.getNextEntry(null, 1)).isNull();
  }

  @Test
  public void dataNotExpected_timestampAfterEntry() throws Exception {
    shadowDropBoxManager.addData(TAG, 1, DATA);

    assertThat(manager.getNextEntry(null, 2)).isNull();
  }

  @Test
  public void dataNotExpected_wrongTag() throws Exception {
    shadowDropBoxManager.addData(TAG, 1, DATA);

    assertThat(manager.getNextEntry(ANOTHER_TAG, 0)).isNull();
  }

  @Test
  public void dataExpectedWithSort() throws Exception {
    shadowDropBoxManager.addData(TAG, 3, DATA);
    shadowDropBoxManager.addData(TAG, 1, new byte[] {(byte) 0x0});

    Entry entry = manager.getNextEntry(null, 2);
    assertThat(entry).isNotNull();
    assertThat(entry.getText(100)).isEqualTo(new String(DATA));
    assertThat(entry.getTimeMillis()).isEqualTo(3);
  }

  @Test()
  public void resetClearsData() throws Exception {
    shadowDropBoxManager.addData(TAG, 1, DATA);

    shadowDropBoxManager.reset();

    assertThat(manager.getNextEntry(null, 0)).isNull();
  }
}
