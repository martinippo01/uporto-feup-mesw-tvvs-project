package pt.feup.tvvs.pacman.viewer;

import org.junit.jupiter.api.Test;
import pt.feup.tvvs.pacman.gui.GUI;
import pt.feup.tvvs.pacman.model.Element;
import pt.feup.tvvs.pacman.model.Position;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class ModelViewerWhiteBoxTests {

    // tiny concrete Element subclass for testing
    static class TestElement extends Element {
        public TestElement(Position pos) {
            super(pos);
        }
    }

    @Test
    public void drawElement_with_element_delegates_to_inner_viewer_if_present() {
        @SuppressWarnings("unchecked")
        Viewer<Element> inner = (Viewer<Element>) mock(Viewer.class);

        Map<Class<?>, Viewer<Element>> map = new HashMap<>();
        map.put(TestElement.class, inner);

        ModelViewer<TestElement> mv = new ModelViewer<>(map) {
            @Override
            public void drawElements(GUI gui, TestElement model, long frameCount) {
                // not used in this test
            }
        };

        GUI gui = mock(GUI.class);
        TestElement e = new TestElement(new Position(1, 2));

        mv.drawElement(gui, (Element) e, 5L);

        verify(inner).drawElement(gui, e, 5L);
    }

    @Test
    public void drawElement_with_element_does_nothing_if_no_viewer() {
        Map<Class<?>, Viewer<Element>> map = new HashMap<>();
        ModelViewer<TestElement> mv = new ModelViewer<>(map) {
            @Override
            public void drawElements(GUI gui, TestElement model, long frameCount) {
            }
        };

        GUI gui = mock(GUI.class);
        TestElement e = new TestElement(new Position(0, 0));

        // should not throw and not call any viewer
        mv.drawElement(gui, (Element) e, 3L);

        // nothing to verify on inner viewers since none exist; just assert no interactions on GUI
        verifyNoInteractions(gui);
    }

    @Test
    public void drawElement_with_model_clears_once_calls_drawElements_and_refresh() throws Exception {
        Map<Class<?>, Viewer<Element>> map = new HashMap<>();

        final boolean[] called = {false};

        ModelViewer<String> mv = new ModelViewer<>(map) {
            @Override
            public void drawElements(GUI gui, String model, long frameCount) {
                called[0] = true;
            }
        };

        GUI gui = mock(GUI.class);
        when(gui.getNextAction()).thenReturn(java.util.List.of()); // unused but harmless

        mv.drawElement(gui, "model", 1L);

        verify(gui).clear();
        verify(gui).refresh();
        assertThat(called[0]).isTrue();

        // second call should not clear again, but should refresh and call drawElements
        called[0] = false;
        mv.drawElement(gui, "model2", 2L);
        verify(gui, times(1)).clear();
        verify(gui, times(2)).refresh();
        assertThat(called[0]).isTrue();
    }

    @Test
    public void drawElement_wraps_ioexception_from_refresh_in_runtimeexception() throws Exception {
        Map<Class<?>, Viewer<Element>> map = new HashMap<>();

        ModelViewer<String> mv = new ModelViewer<>(map) {
            @Override
            public void drawElements(GUI gui, String model, long frameCount) {
                // do nothing
            }
        };

        GUI gui = mock(GUI.class);
        doThrow(new IOException("boom")).when(gui).refresh();

        assertThatThrownBy(() -> mv.drawElement(gui, "m", 1L)).isInstanceOf(RuntimeException.class).hasMessageContaining("boom");

        // ensure clear was still called before refresh
        verify(gui).clear();
        verify(gui).refresh();
    }
}
