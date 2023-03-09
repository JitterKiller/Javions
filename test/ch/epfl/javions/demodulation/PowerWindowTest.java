package ch.epfl.javions.demodulation;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class PowerWindowTest {

    @Test
    public void testGetValidIndex() throws IOException {
        /* Mettre BatchSize à 8 pour tester le changement de tableau, le changement d'index */
        int[] tab = new int[]{73, 292, 65, 745, 98, 4226, 12244, 25722};
        int[] tab1 = new int[]{36818, 23825, 10730, 1657, 1285, 1280, 394, 521};
        int[] tab2 = new int[]{1370, 200, 292, 290, 106, 116, 194, 64};
        int[] tab3 = new int[]{37, 50, 149, 466, 482, 180, 148, 5576};
        InputStream stream = new FileInputStream("resources/Samples.bin");
        int windowSize = 5;
        PowerWindow window = new PowerWindow(stream, windowSize);

        assertEquals(tab[0], window.get(0));
        assertEquals(tab[1], window.get(1));
        assertEquals(tab[2], window.get(2));
        assertEquals(tab[3], window.get(3));
        assertEquals(tab[4], window.get(4));

        window.advanceBy(8);
        assertEquals(tab1[0], window.get(0));
        assertEquals(tab1[1], window.get(1));
        assertEquals(tab1[2], window.get(2));
        assertEquals(tab1[3], window.get(3));
        assertEquals(tab1[4], window.get(4));

        window.advanceBy(8);
        assertEquals(tab2[0], window.get(0));
        assertEquals(tab2[1], window.get(1));
        assertEquals(tab2[2], window.get(2));
        assertEquals(tab2[3], window.get(3));
        assertEquals(tab2[4], window.get(4));

        window.advanceBy(6);
        assertEquals(tab2[6], window.get(0));
        assertEquals(tab2[7], window.get(1));
        assertEquals(tab3[0], window.get(2));
        assertEquals(tab3[1], window.get(3));
        assertEquals(tab3[2], window.get(4));

    }

    @Test
    public void testIsFull() throws IOException {
        InputStream stream = new ByteArrayInputStream(new byte[80]);
        /** 40 représente la taille d'un flot d'octet en entrée,
         * dont découlent 20 échantillons, dont découlent 10 échantillons de puissance
         * donc décaler de 5 la fenêtre la laisse remplie, puis la redécaler entraine qu'elle n'est plus remplie*/
        int windowSize = 5;
        PowerWindow window = new PowerWindow(stream, windowSize);
        window.advanceBy(8);
        assertTrue(window.isFull());

        window.advanceBy(8);
        assertFalse(window.isFull());
    }
}
