/*Tämä on harjoitustyön pohja, joka hoitaa
kuvatiedoston lukemisen, kuvan piirtämisen 
ja tekstikentän, johon kirjoitetaan
tehtävänannossa ilmoitetut tiedot.
Oman Union-Find toteutuksen voi kirjoittaa
luokkana omaan tiedostoonsa tai funktioina 
tähän samaan tiedostoon.
Huomaa, että Testikuva.bmp pitää olla samassa
hakemistossa/kansiossa, kun ajattava ohjelma.
Huomaa, että kuvan koko on "kovakoodattu"
800x600 kuvapistettä.

*/
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;
import java.util.ArrayList;

//Luokka, joka toimii käyttöliittymänä ja kuvatiedoston lukijana.
public class Tira extends JPanel implements ActionListener {
   
    BufferedImage img, res; //img sisältää hakemistosta luettavan kuvan ja res itse piirrettävän kuvan.
    TextField text;         //Tänne kirjoitetaan pyydetyt tilastotiedot kuvan sisältämistä yhdistetyistä komponenteista.
    boolean selection;      //Ilmaisee sen, onko valittuna alkuperäinen vai itse piirretty kuva.
    protected JButton b1;   //Painike, jolla valitaan näytettävä kuva.

    DisjointSet nodet = new DisjointSet(); //n

    ////tähän muuttujaan tallennetaan kuinka monta pikseliä suurimassa yhdistetyssä komponentissa on
    int amountOfPixelsInLargestComponent;
    int amountOfComponents; //tähän muuttujaan tallennetaan kuinka monta yhdistettyä komponenttia kuvassa on
    //tähän muuttujaan tallennetaan yhdistetyn komponentin painopistepikselin koordinaatit. 
    //ensimmäinen numero on x-koordinaatti ja toinen y.
    //Tämän sijaan olisi voinut käyttää Point-luokkaa, mutta en jaksa koska tätä käytetään vain yhden kerran.
    int[] centreOfGravityPixelCoords;

    
    //Suorittaa kuvan piirtämisen valinnan mukaan.
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (selection){
            g.drawImage(res, 0, 0, null);
            text.setText(amountOfComponents + "," + amountOfPixelsInLargestComponent
                + ",(" + centreOfGravityPixelCoords[0] + "," + centreOfGravityPixelCoords[1] + ")");
        }
        if(!selection){
            g.drawImage(img, 0, 0, null);  
            text.setText("Original image");
        }
    }

    //Rakennin, joka lataa kuvan ja asettaa käyttöliittymäkomponentit.
    public Tira() {

        try {
            img = ImageIO.read(new File("Testikuva.bmp"));
        } catch (IOException e) {
        }

        text = new TextField(20);        
        b1=new JButton("Change Image");        
        b1.setEnabled(true);
        b1.addActionListener(this);
        b1.setVisible(true);    
        add(b1);
        add(text);       
        selection=false;
    }

    //Tätä funktiota kutsutaan, kun painetaan nappia.
    public void actionPerformed(ActionEvent e) {
        selection=!selection;
        processImage(); //Esimerkkifunktio kuvan käsittelyä ja Union-Find rakenteen kutsua varten.
        repaint();
    }

    public Dimension getPreferredSize() {
        if (img == null) {
             return new Dimension(100,100);
        }else{
           return new Dimension(img.getWidth(null), img.getHeight(null));
       }
    }


    public void processImage(){

        int[][] labels = new int[800][600];
        int temp;

        res=new BufferedImage(800,600,BufferedImage.TYPE_BYTE_BINARY);
        int pixelvalue;
        int connectedComponentIndex = 1;

        //ensimmäinen läpikäynti: käydään läpi pikseli pikseliltä, annetaan pikseleille tunnistenumerot (label)
        //ja otetaan ylös yhdistettyjen alueiden labelit
        for(int y = 0; y < 600; y++){
            for(int x = 0; x < 800; x++){

                //jos tarkasteltava pikseli ei ole musta, suoritetaan sen tarkastaminen
                // ja annetaan sille tunnistenumero (label)
                if(!pixelIsBlack(x, y, img)){

                    //valkoisten naapureiden indeksit
                    int[] neighbourIndexes = getPixelsNeighboursIndexes(x, y, labels);
                    //pienin indeksi naapureista
                    int smallestIndex = getSmallestIndexFromArray(neighbourIndexes);

                    //ifiin mennään jos ei löydetä valkoisia (edellisiä) naapureita
                    if(smallestIndex == 0){
                        //luodaan uusi alkio, jolla on vanhempana se itse ja arvona (rank) 0
                        nodet.makeSet(connectedComponentIndex);
                        labels[x][y] = connectedComponentIndex;
                        connectedComponentIndex++;


                    }else{
                        //tässä pistetään pienin indeksi uuden labeliksi
                        labels[x][y] = smallestIndex;
                        //käydään läpi naapurit
                        for(int i = 0; i < neighbourIndexes.length; i++){
                            //jos naapuri ei ole taustaa
                            if(neighbourIndexes[i] != 0){
                                //unionilla yhdistetään kaksi puuta, toinen joka sisältää pienimmän indeksin
                                //ja toinen joka sisältää naapurin indeksin
                                nodet.union(neighbourIndexes[i], smallestIndex);
                            }
                        } 
                    }
                }
            }
        }

        temp = 255;
        temp = temp<<8;

        //toinen läpikäynti, yhdistetään alueet
        for(int y = 0; y < 600; y++){
            for(int x = 0; x < 800; x++){
                //jos tarkasteltava pikseli ei ole musta siirrytään tarkasteluun
                if(!pixelIsBlack(x, y, img)){
                    //holderi pitämään halutun setin roottia. Tämän saisi varmaan paremminkin tehtyä.
                    Object holder = nodet.find(labels[x][y]);
                    //ja tuosta asetetaan setin rootti labeliksi
                    labels[x][y] = (Integer) holder;
                }
            }
        }

        //tässä haetaan kaikista suurin komponentti
        int mostFrequent = findBiggestComponent(labels);

        //ja tässä for-for:issa asetetaan suurimman komponenttien kohtiin valkoista. Muut jää taustaksi.
        for(int y = 0; y < 600; y++){
            for(int x = 0; x < 800; x++){
                if(labels[x][y] == mostFrequent){
                    res.setRGB(x,y,temp);
                }
            }
        }

        //haetaan suurimman (mostFrequent) yhdistetyn komponentin kuvapisteiden määrä
        amountOfPixelsInLargestComponent = findAmountOfPixelsInComponent(mostFrequent, labels);

        //haetaan yhdistettyjen komponenttien määrä
        amountOfComponents = findAmountOfComponents(labels);

        centreOfGravityPixelCoords = getCentreOfGravity(mostFrequent, labels);
    }

    /*
    * Hakee painopisteen. Parametreina annetaan sen yhdistetyn komponentin indeksi jonka painopiste
    * halutaan hakea, ja taulukko jossa indeksit.
    * Palauttaa painopisteen koordinaatit int[2] tauluna
    *
    */
    public int[] getCentreOfGravity(int index, int[][] labels){
        //tähän lätkitään koordinaatit, eka numero (indeksi 0) on x-koordinaatti ja toka on y.
        //tämän sijaan olis esim voinut käyttää Point-luokkaa,
        // en jaksanut hakea kun käytetään vain kerran.
        int[] coordsOfCentre = new int[2];

        int centreX = 0, centreY = 0, totalmass = 0;

        for(int y = 0; y < 600; y++){
            for(int x = 0; x < 800; x++){
                if(labels[x][y] == index){
                    totalmass += 1;
                    //ykköset täytyy lisätä koska taulukon indeksit periaatteessa kusee yhdellä.
                    centreX += x+1;
                    centreY += y+1;
                }
            }
        }

        //
        coordsOfCentre[0] = Math.round(((float) centreX) / ((float)totalmass));
        coordsOfCentre[1] = Math.round(((float) centreY) / ((float)totalmass));

        return coordsOfCentre;
    }

    /*
    * Tässä haetaan että kuinka monta kertaa annettu numero (index)
    * sisältyy annettuun taulukkoon (labels).
    * Tämän olisi voinut helposti yhdistää findBiggestComponenttiin, mutta palautusarvon muodosta
    * olisi tullut outo.
    * Tää on tosi yksinkertanen.
    */
    public int findAmountOfPixelsInComponent(int index, int[][] labels){
        int amount = 0;

        for(int y = 0; y < 600; y++){
            for(int x = 0; x < 799; x++){
                if(labels[x][y] == index){
                    amount++;
                }
            }
        }

        return amount;
    }

    /*
    * Etsii kuinka monta eri numeroa kaksi-ulotteisessa taulukossa on.
    * Jos arvo on 0, sitä ei oteta huomioon (taustaa)
    * 
    */
    public int findAmountOfComponents(int[][] labels){

        //tähän tallennetaan aina uusi numero, kun sellainen ilmentyy.
        ArrayList<Integer> numbers = new ArrayList<Integer>();
        //tämä amount suht turha, kun oikeastaan numeroiden määrän
        //pystyisi katsoa lopuksi numbers-listasta
        //en jaksa korjata.
        int amount = 0;
        boolean isAlreadyfound = false;

        //ja tässä for-forissa käydään jokainen taulukon numero läpi
        for(int x = 0; x < 799; x++){
            for(int y = 0; y < 599; y++){
                if(labels[x][y] != 0){ //jos löytyy ei-taustaa...

                    isAlreadyfound = false;

                    //käydään läpi jo tallennetut numerot
                    for (Integer s : numbers){
                        //jos huomataan että numero on jo listassa
                        //muutetaan lippua jotta sitä ei lisätä uudestaan
                        if (s.equals(labels[x][y])){
                            isAlreadyfound = true;
                        }
                    }

                    //jos numeroa ei löytynyt listasta
                    if(!isAlreadyfound){
                        //numero lisätään listaan
                        numbers.add(labels[x][y]);
                        //ja numeroiden määrää kasvatetaan.
                        amount++;
                    }
                }
            }
        }

        return amount;
    }

    /*
    * Hakee kaksiulotteisesta int-taulukosta sen numeron (joka on suurempi kuin 0),
    * joka esiintyy siinä useimmiten.
    * param a = taulukko josta numero haetaan
    * return = numero (> 0) joka esiintyy useiten taulukossa
    */
    public int findBiggestComponent(int[][] a){
        //tähän tallennetaan numeroiden määrät
        Map<Integer, Integer> counts = new HashMap<Integer, Integer>();

        for(int i = 0; i < a.length; i++){
            for(int j = 0; j < a[i].length; j++){
                if(a[i][j] > 0){ // jos numero ei ole taustaa
                    if(counts.containsKey(a[i][j])){ //tänne jos numeroa on ollut aiemmin
                        int count = counts.get(a[i][j]);
                        count++;
                        counts.put(a[i][j], count);
                    }else{ //tähän mennään jos numero löytyy vasta ensimmäistä kertaa
                        counts.put(a[i][j], 1);
                    }
                }
            }
        }

        int biggest = 0;
        int biggestKey = 0;

        //ja käydääs läpi
        for (Map.Entry<Integer, Integer> entry : counts.entrySet()){
            if(entry.getValue() > biggest){
                biggestKey = entry.getKey();
            }
        }

        return biggestKey;
    }

    //palauttaa 0 jos ei naapureita, naapureiden pienemmän indeksin jos on
    public int getSmallestIndexFromArray(int[] array){

        int smallest = 0;

        for(int i = 0; i < array.length; i++){
            if(smallest == 0){
                smallest = array[i];
            }else if(array[i] < smallest && array[i] != 0){
                smallest = array[i];
            }
        }
        return smallest;

    }

    //tarkistaa onko taulukossa pelkästään nollia vai ei
    public boolean arrayHasOnlyZeroes(int[] array){
        for(int i = 0; i < array.length; i++){
            if(array[i] != 0){
                return false;
            }
        }

        return true;
    }

    //palauttaa taulukon jossa on naapureiden (4 edellisen) indeksit
    public int[] getPixelsNeighboursIndexes(int x, int y, int[][] labels){

        int[] indexes = new int[4];

        if(x != 0 && y != 0){
            //tarkistaa "North-West:in", taulukkoarvo 1
            indexes[1] = labels[x-1][y-1];
        }
        if(x != 0){
            //tarkistaa "West:in", taulukkoarvo 3
            indexes[0] = labels[x-1][y];
        }
        if(y != 0){
            //tarkistaa "North:in", taulukkoarvo 3
            indexes[2] = labels[x][y-1];
            //tarkistaa "North-Eastin:in", taulukkoarvo 4
            if(x != 799){ 
                indexes[3] = labels[x+1][y-1];
            }
        }

        return indexes;
    }

    //tarkastaa onko annetusta kuvasta annettu pikseli (x-y-parametrien avulla) musta
    //jos on, palauttaa true, jos ei, false
    public boolean pixelIsBlack(int x, int y, BufferedImage image){
        int pixelvalue = image.getRGB(x,y);
        int temp = pixelvalue>>16&0xff;

        if(temp == 0){
            return true;
        }else{
            return false;
        }
    }

    public static void main(String[] args) {

        //Ohjelman pääikkuna.
        JFrame f = new JFrame("Tira Harkka");

        //Lisätään pääikkunaan sulkemismahdollisuus.      
        f.addWindowListener(new WindowAdapter(){
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
        });

        //Luodaan oman ohjelman esiintymä.
        Tira t=new Tira();     
        //Lisätään se ikkunaan.	   
        f.add(t);
        //Määrittää ikkunan koon.
        f.pack();
        //Näyttää ohjelmaikkunan.
        f.setVisible(true);
    }
}
