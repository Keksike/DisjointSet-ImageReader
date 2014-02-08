
import java.util.HashMap;
import java.util.Iterator;

/*
* Tietorakenne, joka pitää sisällään ns. disjoint-set foresteja, ja union-find algoritmin
*
* Lähteenä vahvasti käytetty: 
* Cormenin, Leisersonin, Rivestin ja Steinin "Introduction to Algorithms: Third Edition"
*/
public class DisjointSet{

	/*
	* Luo uuden setin. Uuden noden default vanhemmaksi se itse
	* ja rankiksi ("arvoksi") 0
	*/
	public void makeSet(Object object){
		nodes.put(object, new Node(object, 0));
	}

	//kartta johon voidaan tallentaa objektista nodeiksi
	private final HashMap nodes = new HashMap();


	/*
	* Node-luokka toimii tietoalkiona, joka pitää sisällään tarvittavat tiedot
	* näitä Nodeja sitten yhdistetään toisiinsa parent-arvon avulla
	*
	**/
	private static class Node{
		//alkion vanhempi
		Object parent;
		//"arvo" jota käytetään unioni-optimisaatiossa
		int rank;

		Node(Object parent, int rank) {
			this.parent = parent;
			this.rank = rank;
		}
	}

	/*
	* find etsii sen setin juuren, jossa annettu objekti on, ja palauttaa sen
	* Melko yksinkertainen, eipä selitettävää.
	*
	*/
	public Object find(Object object){
		DisjointSet.Node node = (DisjointSet.Node) nodes.get(object);

		if(node == null){
			return null;
		}

		//jee rekursiivisyyttä
		if(node.parent != object){
			node.parent = find(node.parent);
		}

		return node.parent;
	}

	/*
	* Union yhdistää kaksi settiä, jotka sisältävät alkiot i ja j.
	* Parametrien ei siis tarvitse olla juuria, metodi hakee juuret. (helpottaa käyttöä)
	* Käyttää rankkeja optimointiin.
	*/
	public void union(Object i, Object j){
		Object setI = find(i);
		Object setJ = find(j);

		//"virhetilanteissa" ei tehdä mitään
		if(setI == null || setJ == null || setJ == setI){
			return;
		}

		DisjointSet.Node nodeI = (DisjointSet.Node) nodes.get(setI);
		DisjointSet.Node nodeJ = (DisjointSet.Node) nodes.get(setJ);

		if(nodeI.rank > nodeJ.rank){
			nodeJ.parent = i;
		}else{
			nodeI.parent = j;
			if(nodeI.rank == nodeJ.rank){
				nodeJ.rank++;
			}
		}
	}
}