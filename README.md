# Programa necessário
Java
# Sistema operacional
Windows
# Como executar
Para compilar, use o argumento:
`-cp .;%MPJ_HOME%/lib/mpj.jar`

Na pasta ??? do projeto execute no terminal:
`mpjrun.bat -np 4 trabalho2PC.java`

- tamanho_da_base é o tamanho da base que pode ser 59, 161, 256, 1380, 1601, com 59 sendo o valor default.
- tipo identifica qual o modo de execução: se paralelo(1) ou sequencial(0). O modo padrão é o sequencial.
- quantidade_de_threads é o número de threads que srão utilizadas na execução paralela, podendo ser de 1 ao infinito. Caso seja inserido qualquer outro valor, ou se for inserida uma letra, ou não se inserir nada quando escolhido o modo paralelo de execução, ocorrerá um erro.

Caso nenhum parametro seja fornecido, será executada a base 59 de forma sequencial.