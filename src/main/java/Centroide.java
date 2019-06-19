import mpi.MPI;

import java.util.ArrayList;
import java.util.List;

class Centroide extends Elemento {

    private List<Thread> threads = new ArrayList<>();

    Centroide(int numD, int[] attr) {
        super(numD, attr);
    }

    private boolean setAtributo(int i, int attr) {
        boolean mudado = atributos[i] != attr;
        atributos[i] = attr;
        return mudado;
    }

    boolean recalculaAtributos(List<Elemento> elementos) {
        int num;
        int soma;
        int i;
        boolean mudado = false;
        for (i = 0; i < numDimensoes; i++) {
            soma = 0;
            num = 0;
            for (Elemento elemento : elementos) {
                if (elemento.getAssociado() == this) {
                    num++;
                    soma += elemento.getAtributo(i);
                }
            }
            if (num > 0) {
                if (setAtributo(i, soma / num)) {
                    mudado = true;
                }
            }
        }
        return mudado;
    }

    boolean recalculaAtributosPar(List<Elemento> elementos) throws InterruptedException {
        int eu= MPI.COMM_WORLD.Rank();
        int tamanho= MPI.COMM_WORLD.Size();
        if (eu == 0){
            for(int i= 0; i<tamanho; i++){

            }
        }else{

        }
        return false;
    }
}