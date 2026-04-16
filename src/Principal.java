import java.util.Scanner;

import entidades.usuarios.*;
import entidades.cursos.*;

public class Principal {
    public static void main(String[] args) {
        System.out.println("===========================");
        System.out.println("  Menu Inicial - Grupo 12  ");
        System.out.println("===========================");

        ArquivoUsuario arqUsuario = null;
        ArquivoCurso arqCurso = null;
        Scanner console = new Scanner(System.in);

        try {
            // 1. Abre os arquivos (uma instância de cada)
            arqUsuario = new ArquivoUsuario();
            arqCurso = new ArquivoCurso();

            // 2. Cria os controladores com as instâncias compartilhadas
            ControleUsuario ctrlUsuario = new ControleUsuario(arqUsuario, arqCurso);
            ControleCurso ctrlCurso = new ControleCurso(arqCurso);

            // 3. Cria as visões com os controladores e scanner compartilhados
            VisaoCurso visaoCurso = new VisaoCurso(ctrlCurso, console);
            VisaoUsuario visaoUsuario = new VisaoUsuario(ctrlUsuario, visaoCurso, console);

            // 4. Inicia o loop do menu
            visaoUsuario.menuUsuario();

        } catch (Exception e) {
            System.err.println("Erro critico ao inicializar os arquivos do sistema:");
            System.err.println(e.getMessage());
            e.printStackTrace();
        } finally {
            // FIX: Fecha todos os arquivos ao sair, mesmo se der exceção.
            // Antes nunca chamava close(), podendo truncar escritas pendentes.
            try {
                if (arqCurso != null) arqCurso.close();
                if (arqUsuario != null) arqUsuario.close();
            } catch (Exception e) {
                System.err.println("Erro ao fechar arquivos: " + e.getMessage());
            }
            console.close();
        }

        System.out.println("Sistema encerrado. Ate mais!");
    }
}
