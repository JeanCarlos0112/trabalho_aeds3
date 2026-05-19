# Guia de Estrutura e Integração — TP1 AEDs III

## Estrutura de Diretórios Recomendada

```
projeto/
├── Videos/
│   └── VideoTP1_G12.mp4                       ← Vídeo de demonstração
├── src/
│   ├── aed3/                                  ← Pacote do professor (não alterar)
│   │   ├── Arquivo.java                       ← CRUD genérico
│   │   ├── ArvoreBMais.java                   ← Árvore B+
│   │   ├── HashExtensivel.java                ← Hash Extensível
│   │   ├── InterfaceArvoreBMais.java
│   │   ├── InterfaceEntidade.java
│   │   ├── InterfaceHashExtensivel.java
│   │   ├── ParIDEndereco.java                 ← Par (id, endereço) — índice direto
│   │   ├── ParIdId.java                       ← Par (id1, id2) — relacionamento 1:N
│   │   └── ParNomeId.java                     ← Par (nome, id) — índice por nome
│   │
│   ├── entidades/
│   │   ├── usuarios/
│   │   │   ├── Usuario.java                   ← Entidade (MIRO)
│   │   │   ├── ArquivoUsuario.java            ← CRUD + índice email (MIRO + JEAN)
│   │   │   ├── ParEmailId.java                ← Par (email, id) para hash (JEAN)
│   │   │   ├── VisaoUsuario.java              ← View — E/S de dados (LUIZ)
│   │   │   └── ControleUsuario.java           ← Controller — lógica + menu (LUIZ)
│   │   │
│   │   └── cursos/
│   │       ├── Curso.java                     ← Entidade (MIRO)
│   │       ├── ArquivoCurso.java              ← CRUD + B+ 1:N + Hash código (JEAN)
│   │       ├── ParCodigoId.java               ← Par (código, id) p/ hash NanoID (JEAN, TP2)
│   │       ├── VisaoCurso.java                ← View — E/S de dados (ANDRÉ + JEAN no TP2)
│   │       └── ControleCurso.java             ← Controller — lógica + menu (ANDRÉ + JEAN no TP2)
│   │
│   ├── Principal.java                         ← Main (menu de login + menu principal)
│   ├── TesteRelacionamento1N.java             ← Teste de regressão do TP1 (23 verificações)
│   └── TesteBuscaCursos.java                  ← Teste da Busca de Cursos do TP2 (20 verificações)
│
└── dados/                                     ← Gerado automaticamente em runtime
    ├── usuarios/
    │   ├── dados.db
    │   ├── indiceDireto.d.db
    │   ├── indiceDireto.c.db
    │   ├── indiceEmail.d.db
    │   └── indiceEmail.c.db
    └── cursos/
        ├── dados.db
        ├── indiceDireto.d.db
        ├── indiceDireto.c.db
        ├── indiceUsuarioCurso.btree.db        ← Árvore B+ do relacionamento 1:N
        ├── indiceNomeCurso.btree.db           ← Árvore B+ por nome (ordem alfabética)
        ├── indiceCodigo.d.db                  ← Hash Extensível por NanoID (TP2)
        └── indiceCodigo.c.db
```

## O que o **JEAN** entregou (relacionamento 1:N)

### 1. `ParEmailId.java` → `entidades/usuarios/`

Implementação completa do par (email, id) para a Hash Extensível.
Tamanho fixo de 54 bytes (50 para email + 4 para id).
O `ArquivoUsuario` usa isso para buscar usuário pelo email no login.

### 2. `ArquivoCurso.java` → `entidades/cursos/`

Substitui o arquivo do Miro. Contém dois índices Árvore B+:

- **`indiceUsuarioCurso`** — `ParIdId(idUsuario, idCurso)` — É O RELACIONAMENTO 1:N.
- **`indiceNomeCurso`** — `ParNomeId(nomeCurso, idCurso)` — Para listar em ordem alfabética.

Métodos novos disponíveis para o André e o Luiz usarem:

| Método                             | O que faz                                               | Quem usa     |
|------------------------------------|---------------------------------------------------------|--------------|
| `readAll(int idUsuario)`           | Retorna ArrayList com todos os cursos do usuário        | André        |
| `readAllOrdenadoPorNome(int idUsr)`| Igual ao acima, mas em ordem alfabética                 | André        |
| `verificaUsuarioTemCursos(int id)` | Retorna true/false se o usuário tem cursos cadastrados  | Luiz         |

Os overrides de `create`, `delete` e `update` mantêm os índices sincronizados
automaticamente.

### 3. `ArquivoUsuario.java` → `entidades/usuarios/`

Substitui o arquivo do Miro. Adicionei overrides de `delete` e `update` para
manter o índice de email sincronizado (o Miro só tinha `create` e `readEmail`).

---

## Como funciona o 1:N na prática

O `ParIdId` do professor tem um truque no `compareTo`:

```java
public int compareTo(ParIdId a) {
    if (this.id1 != a.id1)
        return this.id1 - a.id1;
    else {
        if (this.id2 == -1)   // ← CORINGA: -1 casa com qualquer id2
            return 0;
        else
            return this.id2 - a.id2;
    }
}
```

Então quando o André chamar `readAll(3)`, internamente acontece:

1. Cria `ParIdId(3, -1)` como chave de busca
2. A Árvore B+ percorre as folhas comparando
3. Como `id2 == -1`, o `compareTo` retorna 0 para **QUALQUER** par que tenha `id1 == 3`
4. Retorna todos: `(3,1)`, `(3,5)`, `(3,12)` — ou seja, cursos 1, 5 e 12 do usuário 3

---

## O que o **LUIZ** entregou (Interface e Controle de Usuário) 

### 1. ControleUsuario.java → entidades/usuarios/ 

Funciona como o "cérebro" do sistema de usuários. Fazendo a ponte entre a interface e o arquivo de dados. Implementa as regras as seguintes regras: 
- Validação de Email — Garante que dois usuários não tenham o mesmo email no cadastro ou na atualização. 
- Segurança — Gerencia o login e a recuperação de senha comparando hashes, garantindo que a senha real nunca seja manipulada em texto aberto. 
- Lógica de Exclusão — Implementa a trava de segurança; se o usuário tiver cursos ativos, a exclusão é abortada. 

### 2. VisaoUsuario.java → entidades/usuarios/ 

Sistema de menus dinâmico que gerencia o estado da sessão (usuário logado e deslogado). 
- Menu Deslogado — Cadastro, Login e Recuperação de Senha. 
- Menu Logado — Visualização, alteração de dados e exclusão de conta. 
- UX de Recuperação — Diferente de um CRUD comum, a visão primeiro busca a pergunta secreta do usuário para só após a resposta correta do usuário para depois permitir a alteração da senha.

| Método                             | O que faz                                               |
|------------------------------------|---------------------------------------------------------|
| `cadastrarUsuario` | Valida email único e cria nova entidade.         |
| `recuperarSenha `| Valida resposta secreta e sobrescreve o hash da senha.             |
| `excluirUsuario ` | Executa a limpeza em cascata (remove cursos inativos) após validação.  |
| `obterPerguntaSecreta ` | Busca apenas a string da pergunta antes da validação da resposta.   |

## Como funciona a Exclusão Segura na prática 

```java
public boolean excluirUsuario(int idUsuario) throws Exception {
    // 1. Bloqueia se houver cursos ativos
    if (arqCurso.verificaUsuarioTemCursos(idUsuario)) {
        return false;   // A exclusão não é executada e retorna false 
    }

    // 2. Remove todos os cursos inativos associados  ao usuário
    ArrayList<Curso> cursosDoUsuario = arqCurso.readAll(idUsuario);
    for (Curso c : cursosDoUsuario) {
        if (c.getEstado() == 2 || c.getEstado() == 3) {
            arqCurso.delete(c.getID());
        }
    }

    // 3. Remove o usuário
    return arqUsuario.delete(idUsuario);
}
```

A lógica que implementei no excluirUsuario garante a integridade do banco de dados evitando que um usuário "suma" deixando cursos ativos pendentes. O fluxo funciona assim: 
1. Primeiro, chamo o método do Jean arqCurso.verificaUsuarioTemCursos(id). Se ele retornar true, eu interrompo tudo e aviso o usuário que ele tem pendências. 
2. Se não houver cursos ativos, o código recupera todos os cursos vinculados àquele ID via readAll(idUsuario). 
3. Eu percorro a lista e deleto apenas os cursos que estão nos estados 2 (Concluído) ou 3 (Cancelado). 
4. Ao final, o arqUsuario.delete aciona o índice do Jean, removendo o email da Hash Extensível automaticamente.

---

## O que o **ANDRÉ** entregou (Interface e Controle de Cursos)

### 1. ControleCurso.java → entidades/cursos/
- Geração de NanoID — Cria automaticamente um código identificador de 10 caracteres alfanuméricos para cada curso, garantindo uma referência pública segura e única.
- Vínculo de Propriedade (FK) — Garante que todo curso criado seja obrigatoriamente associado ao ID do usuário que está logado, mantendo a integridade do relacionamento 1:N.
- Gestão de Estados — Controla a lógica de transição entre os estados de progresso (Pendente, Ativo e Concluído) antes da gravação no arquivo.

### 2. VisaoCurso.java → entidades/cursos/
Sistema de interface via console focado na gestão do catálogo de estudos do usuário.
- Listagem Ordenada — Diferente de uma leitura sequencial, utiliza o índice secundário de nomes para apresentar os cursos em ordem alfabética diretamente para o estudante.
- UX de Gestão — Centraliza as operações de cadastro, atualização de descrições e alteração de status em um menu intuitivo.
- Segurança na Camada de Visão — Valida a propriedade do curso antes de permitir edições ou exclusões, impedindo que um usuário manipule dados de terceiros.

| Método | O que faz |
| :--- | :--- |
| `cadastrarCurso` | Gera o NanoID, define a data atual e vincula o curso ao usuário ativo. |
| `listarCursosOrdenados` | Recupera a lista filtrada por usuário e organizada alfabeticamente via Árvore B+. |
| `atualizarCurso` | Permite editar nome, descrição e estado, preservando o NanoID e a data original. |
| `excluirCurso` | Remove o registro e limpa automaticamente os índices secundários de nome e usuário. |

## Como funciona a Geração de Código e Listagem na prática

```java
public int cadastrarCurso(int idUsuario, String nome, String descricao) throws Exception {
    // 1. Geração do NanoID de 10 caracteres
    String alfabeto = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    StringBuilder nanoid = new StringBuilder();
    Random rnd = new Random();
    for (int i = 0; i < 10; i++) {
        nanoid.append(alfabeto.charAt(rnd.nextInt(alfabeto.length())));
    }

    // 2. Criação do objeto com estado inicial 0 (Pendente)
    Curso novo = new Curso(idUsuario, nome, descricao, LocalDate.now(), nanoid.toString(), 0);
    
    // 3. Persistência e atualização automática dos índices do Jean
    return arqCurso.create(novo);
}
```

---

## O que o **JEAN** entregou (TP2 — Busca de Cursos)

A Busca de Cursos é a pré-condição para o relacionamento N:N (entidade
`CursoUsuario`) e o CRUD de Inscrições, que serão implementados na próxima
etapa do TP2. Esta entrega cobre todas as buscas e a navegação até o curso
escolhido; só falta a efetivação da inscrição em si.

### 1. `ParCodigoId.java` → `entidades/cursos/`

Par (codigo, idCurso) para Hash Extensível. Tamanho fixo de 14 bytes
(10 do código NanoID + 4 do id). Foi escolhido Hash Extensível em vez de
B+ porque a busca por código é sempre por igualdade exata (não há ordenação
nem range query): o usuário cola o NanoID inteiro e quer o curso direto.
Mesma família de índice usada pelo `ParEmailId` no `ArquivoUsuario`.

### 2. `ArquivoCurso.java` (alteração)

Adicionado o terceiro índice `indiceCodigoCurso` (Hash Extensível) ao lado
das duas B+ existentes. Mantido sincronizado em `create`, `delete` e
`update`. Novos métodos públicos:

| Método                          | O que faz                                                     |
|---------------------------------|---------------------------------------------------------------|
| `readByCodigo(String codigo)`   | Busca por NanoID via hash + confirma igualdade exata          |
| `readAllCursos()`               | Varre o `dados.db` direto e retorna todos os cursos válidos   |

O `readAllCursos` precisou de uma abordagem específica: a base `Arquivo`
do prof não expõe iteração e o campo `RandomAccessFile arquivo` é
package-private em `aed3`. Para não tocar no código do professor, abri um
leitor próprio em modo `r` (read-only) sobre o mesmo `dados.db`, respeitando
o cabeçalho de 12 bytes e o esquema de lápides (`' '` válido / `'*'`
excluído). É seguro em ambiente single-thread, que é o caso do programa.

### 3. `ControleCurso.java` (alteração)

Construtor agora recebe `ArquivoUsuario` além de `ArquivoCurso`, necessário
para resolver o nome do autor na tela de detalhe da busca. Novos métodos:

| Método                              | O que faz                                                       |
|-------------------------------------|-----------------------------------------------------------------|
| `buscarPorCodigo(String codigo)`    | Atalho para `arqCurso.readByCodigo`, com `trim()` e null-safe   |
| `listarTodosCursosDisponiveis()`    | Lê todos os cursos, filtra estado 0 e ordena por data de início |
| `buscarAutor(int idUsuario)`        | Resolve o `Usuario` autor de um curso para exibição no detalhe  |

Aproveitei para trocar o `java.util.Random` por `java.security.SecureRandom`
na geração do NanoID (padrão NanoID real) e adicionar dedup contra o
índice de código antes de aceitar o NanoID gerado.

### 4. `VisaoCurso.java` (alteração)

Adicionado o menu "Minhas Inscrições" e as três telas da busca, encadeadas
conforme a especificação do TP2:

| Método (novo)                          | Tela / função                                                     |
|----------------------------------------|-------------------------------------------------------------------|
| `menuInscricoes(int idUsuarioLogado)`  | Menu raiz "Minhas Inscrições" com (A)(B)(C)(R)                    |
| `telaBuscaPorCodigo(int idUsr)`        | Pede o código e abre o detalhe direto se achar                    |
| `telaListaCursos(int idUsr)`           | Lista paginada (10/pág, item 10 → `(0)`), ordenada por data       |
| `telaDetalheCursoVisitante(...)`       | CÓDIGO, CURSO, AUTOR, DESCRIÇÃO, DATA + botão condicional         |

O botão **(A) Fazer minha inscrição no curso** só aparece quando o curso
está em estado 0 (recebendo inscrições) e o visitante não é o dono. A
efetivação imprime, por enquanto, uma mensagem indicando que será
implementada junto com o N:N — é o próximo passo do TP2.

### 5. `VisaoUsuario.java` (alteração)

O placeholder `"(Minhas inscricoes sera implementado no TP2.)"` foi
substituído pelo wire correto: agora `case "C"` chama
`visaoCurso.menuInscricoes(usuarioLogado.getID())`.

### 6. `Principal.java` (alteração)

`ControleCurso` agora é instanciado com `(arqCurso, arqUsuario)` para
ter acesso ao arquivo de usuários e poder resolver o nome do autor.

### 7. `TesteBuscaCursos.java` → `src/`

Arquivo de teste com 20 verificações, cobrindo:
1. Geração de NanoID (10 caracteres, únicos entre cursos)
2. Busca por código retorna o curso certo
3. Códigos inexistente / vazio / null retornam null sem explodir
4. `listarTodosCursosDisponiveis` filtra estado 0 corretamente; cursos
   com estado 1 saem da lista mas continuam buscáveis por código
5. Ordenação por data de início crescente
6. Resolução do autor (`buscarAutor`) — caso positivo e id inexistente
7. Sincronização do `indiceCodigoCurso` após `delete`

Executar com:
```bash
javac -d bin -sourcepath src src/TesteBuscaCursos.java
java -cp bin TesteBuscaCursos
```
Resultado esperado: `20/20 testes passaram`. O `TesteRelacionamento1N`
do TP1 continua passando 23/23 (regressão intacta).

---

## Como funciona a Busca por Código na prática

```java
public Curso readByCodigo(String codigo) throws Exception {
    if (codigo == null || codigo.isEmpty())
        return null;

    // 1. Consulta o índice Hash com hash do código
    ParCodigoId par = indiceCodigoCurso.read(Math.abs(codigo.hashCode()));
    if (par == null)
        return null;

    // 2. Confirma igualdade exata para blindar contra colisões teóricas
    //    de hashCode() — dois códigos diferentes podem (em teoria) ter o
    //    mesmo hash, então recuperar o par e checar a string é essencial.
    if (!par.getCodigo().equals(codigo))
        return null;

    // 3. Lê o curso pelo id armazenado no par
    return read(par.getId());
}
```

O fluxo completo do menu Minhas Inscrições, da busca até a tela de detalhe:

1. Usuário escolhe **(A) Buscar curso por código** e cola o NanoID
2. `telaBuscaPorCodigo` chama `ControleCurso.buscarPorCodigo`
3. `ControleCurso` repassa para `ArquivoCurso.readByCodigo`, que consulta
   o `indiceCodigoCurso` e confirma a igualdade exata
4. Se o curso existe, `telaDetalheCursoVisitante` é chamada com o `idCurso`
5. A tela busca o curso por id e o autor por `ControleCurso.buscarAutor`
6. Renderiza CÓDIGO, CURSO, AUTOR, DESCRIÇÃO, DATA, e o botão (A) só se
   o curso está em estado 0 e o visitante não é o dono

