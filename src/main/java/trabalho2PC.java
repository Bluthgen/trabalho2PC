import mpi.Datatype;
import mpi.MPI;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class trabalho2PC{

    private static boolean para = false;
    private final static Charset ENCODING = StandardCharsets.UTF_8;
    private static List<Elemento> elementos = new ArrayList<>();
    private static List<Centroide> centroides = new ArrayList<>();

    private static int[] centro(int num, Scanner scanner) {
        String linha = scanner.nextLine();
        String[] pedacos = linha.split(",");
        int[] atributos = new int[num];
        int j = 0;
        for (String pedaco : pedacos) {
            atributos[j] = Integer.parseInt(pedaco);
            j++;
        }
        return atributos;
    }

    private static void carregaElementos(List<Elemento> lista, int num) throws IOException {
        String fileName = Paths.get("").toAbsolutePath().toString() + "\\res\\int_base_" + num + ".data";
        Path path = Paths.get(fileName);
        int i = 0;
        try (Scanner scanner = new Scanner(path, ENCODING.name())) {
            while (scanner.hasNextLine()) {
                int[] atributos = centro(num, scanner);
                lista.add(i, new Elemento(num, atributos));
                i++;
            }
        }
    }

    private static void carregaCentroide(List<Centroide> lista, int num) throws IOException {
        String fileName = Paths.get("").toAbsolutePath().toString() + "\\res\\int_centroid_" + num + "_20.data";
        Path path = Paths.get(fileName);
        int i = 0;
        try (Scanner scanner = new Scanner(path, ENCODING.name())) {
            while (scanner.hasNextLine()) {
                int[] atributos = centro(num, scanner);
                lista.add(i, new Centroide(num, atributos));
                i++;
            }
        }
    }

    private void k_meansSeq() {
        int numC;
        while (!para) {
            for (Elemento elemento : elementos) {
                elemento.encontraCentroide(centroides);
            }
            numC = 0;
            for (Centroide centroide : centroides) {
                boolean mudado = centroide.recalculaAtributos(elementos);
                if (!mudado) {
                    numC++;
                }
            }
            if (numC == 20) {
                para = true;
            }
        }
    }

    private static void k_meansPar(){
        System.out.println("Entrando "+ MPI.COMM_WORLD.Rank());
        int numC;
        while (!para) {
            for (Elemento elemento : elementos) {
                elemento.encontraCentroide(centroides);
            }
            numC = 0;
            for (Centroide centroide : centroides) {
                boolean mudado = centroide.recalculaAtributosPar(elementos);
                if (!mudado) {
                    numC++;
                }
            }
            if (numC == 20) {
                para = true;
            }
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        MPI.Init(args);
        //nrThread ou rank não sei o melhor da refactor pois coloquei em varios lugares
        int nrThread = MPI.COMM_WORLD.Rank();
        int quantThreads= MPI.COMM_WORLD.Size();
        long startTempo = System.currentTimeMillis();

        /* Seria o correto mas tem q fazer bCast não sei faze isso com classes imagina um list class
        if(nrThread == 0) {
            switch (args[3]) {
                case "161":
                    carregaElementos(elementos, 161);
                    carregaCentroide(centroides, 161);
                    break;

                case "256":
                    carregaElementos(elementos, 256);
                    carregaCentroide(centroides, 256);
                    break;

                case "1380":
                    carregaElementos(elementos, 1380);
                    carregaCentroide(centroides, 1380);
                    break;

                case "1601":
                    carregaElementos(elementos, 1601);
                    carregaCentroide(centroides, 1601);
                    break;

                default:
                    carregaElementos(elementos, 59);
                    carregaCentroide(centroides, 59);
                    break;
            }//System.out.println("FIM "+nrThread);
            MPI.COMM_WORLD.Barrier();
        }else{
            //System.out.println("Entrando "+nrThread);
            MPI.COMM_WORLD.Barrier();
            //System.out.println("Saindo "+nrThread);
        }
         */

            switch (args[3]) {
                case "161":
                    carregaElementos(elementos, 161);
                    carregaCentroide(centroides, 161);
                    break;

                case "256":
                    carregaElementos(elementos, 256);
                    carregaCentroide(centroides, 256);
                    break;

                case "1380":
                    carregaElementos(elementos, 1380);
                    carregaCentroide(centroides, 1380);
                    break;

                case "1601":
                    carregaElementos(elementos, 1601);
                    carregaCentroide(centroides, 1601);
                    break;

                default:
                    carregaElementos(elementos, 59);
                    carregaCentroide(centroides, 59);
                    break;
            }

        if (quantThreads > 1) {
            if(nrThread == 0) {
                System.out.println("Execução Paralela");
                MPI.COMM_WORLD.Barrier();
            }else{
                MPI.COMM_WORLD.Barrier();
            }
            k_meansPar();
        } else {
            System.out.println("Execução Sequencial");
            trabalho2PC trabalho = new trabalho2PC();
            trabalho.k_meansSeq();
        }

        if(nrThread == 1) {
            int i = 0;
            for (Elemento elemento : elementos) {

                System.out.println("Id: " + i + "\t" + "Classe: " + centroides.indexOf(elemento.getAssociado()));
                i++;
            }
            System.out.println("Tempo decorrido: " + (System.currentTimeMillis() - startTempo));
        }else{
            MPI.COMM_WORLD.Barrier();
        }
        MPI.Finalize();
    }
}