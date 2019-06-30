import mpi.Datatype;
import mpi.MPI;

import java.util.List;

class Centroide extends Elemento {

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

    boolean recalculaAtributosPar(List<Elemento> elementos){
        int eu= MPI.COMM_WORLD.Rank();
        int tamanho= MPI.COMM_WORLD.Size();
        Double temp= this.numDimensoes / (tamanho*1.0);
        int numPorThread= (int) Math.ceil(temp);
        int[] recebidos= new int[numPorThread];
        //int[] finais= new int[this.numDimensoes];         Talvez seja necessario
        MPI.COMM_WORLD.Scatter(this.atributos, 0, numPorThread, Datatype.Contiguous(numPorThread,MPI.INT), recebidos, 0, numPorThread, Datatype.Contiguous(numPorThread, MPI.INT), 0);
        boolean mudado= false;
        for (int i = 0; i < numPorThread; i++) {
            int soma = 0;
            int num = 0;
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
}