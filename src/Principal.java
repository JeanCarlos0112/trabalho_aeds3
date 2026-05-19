import java.util.Scanner;
import entidades.usuarios.*;
import entidades.cursos.*;

public class Principal {
    public static void main(String[] args) {
        ArquivoUsuario arqUsuario = null;
        ArquivoCurso arqCurso = null;
        Scanner console = new Scanner(System.in);

        try {
            arqUsuario = new ArquivoUsuario();
            arqCurso = new ArquivoCurso();

            ControleUsuario ctrlUsuario = new ControleUsuario(arqUsuario, arqCurso);
            ControleCurso ctrlCurso = new ControleCurso(arqCurso, arqUsuario);

            VisaoCurso visaoCurso = new VisaoCurso(ctrlCurso, console);
            VisaoUsuario visaoUsuario = new VisaoUsuario(ctrlUsuario, visaoCurso,  ctrlCurso, console);

            visaoUsuario.menuUsuario();

        } catch (Exception e) {
            System.err.println("Erro critico ao inicializar os arquivos do sistema:");
            System.err.println(e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (arqCurso != null) arqCurso.close();
                if (arqUsuario != null) arqUsuario.close();
            } catch (Exception e) {
                System.err.println("Erro ao fechar arquivos: " + e.getMessage());
            }
            console.close();
        }

        System.out.println("\nSistema encerrado. Ate mais!");
    }
}
