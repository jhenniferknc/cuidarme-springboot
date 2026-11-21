# Cuidar.me — API

Backend do Cuidar.me, sistema de gestão para consultórios de psicologia: cadastro de psicólogos, pacientes, atendimentos, prontuários e pagamentos.

## Tecnologias

- Java 17 e Spring Boot 3.4
- Spring Data JPA com PostgreSQL (Neon)
- Bean Validation
- BCrypt (`spring-security-crypto`) para as senhas
- SpringDoc (Swagger)
- JUnit 5, Mockito e JaCoCo

## Como rodar

1. Copie o arquivo de exemplo e preencha com as credenciais do banco:

   ```bash
   cd spring-boot-cuidarme
   cp .env.example .env
   ```

   O `.env` está no `.gitignore`.

2. Suba o servidor:

   ```bash
   ./mvnw spring-boot:run
   ```

3. Acesse:
   - API: `http://localhost:8082/cuidarme/api/`
   - Swagger: `http://localhost:8082/cuidarme/api/swagger-ui/index.html`

Para testar pelo celular na mesma rede Wi-Fi, use o IP do computador no lugar de `localhost` (ex.: `http://192.168.0.10:8082/cuidarme/api/`).

### Variáveis de ambiente

| Variável | Obrigatória | Para que serve |
|---|---|---|
| `DB_URL` | sim | URL JDBC do PostgreSQL |
| `DB_USERNAME` | sim | Usuário do banco |
| `DB_PASSWORD` | sim | Senha do banco |
| `PORT` | não | Porta do servidor (padrão: `8082`) |
| `MOSTRAR_SQL` | não | Mostra o SQL no console (padrão: `true`; no Docker: `false`) |
| `CORS_ORIGENS` | não | Origens web permitidas, separadas por vírgula |

## Testes

```bash
./mvnw test
```

Os testes de controller e de service usam mocks. Só o `TodoApplicationTests` conecta no banco, por isso o `.env` precisa existir.

## Principais endpoints

Todos começam com `/cuidarme/api`.

| Método | Caminho | O que faz |
|---|---|---|
| POST | `/psicologos/cadastrar` | Cadastra psicólogo |
| POST | `/psicologos/login` | Login (401 se e-mail ou senha estiverem errados) |
| GET | `/psicologos/pacientes/{psicologoId}` | Pacientes do psicólogo |
| GET | `/psicologos/atendimentos/{psicologoId}` | Atendimentos do psicólogo |
| POST | `/psicologos/cadastrar-pacientes/{psicologoId}` | Cadastra paciente |
| PATCH | `/pacientes/atualizar/{pacienteId}` | Atualiza paciente |
| DELETE | `/pacientes/remover/{pacienteId}` | Remove paciente (e os atendimentos dele) |
| POST | `/pacientes/cadastrar-atendimentos/{pacienteId}` | Cadastra atendimento |
| PATCH | `/atendimentos/remarcar/{atendimentoId}` | Remarca (nova data e local) |
| PATCH | `/atendimentos/cancelar/{atendimentoId}` | Cancela |

A lista completa com exemplos está no Swagger.

## Regras para o app offline

O app mobile grava tudo primeiro no celular e envia para o servidor quando há internet, em uma fila. Por isso a API segue estas regras:

- **O app escolhe o ID.** O cadastro de paciente e de atendimento aceita `lookupId` (UUID) no corpo. Se não vier, o servidor gera um.
- **Reenviar não duplica.** Um cadastro com um `lookupId` que já existe devolve o registro existente com **200** (um cadastro novo devolve **201**).
- **Remover e cancelar de novo não dá erro.** Remover um paciente que já não existe devolve **204**. Cancelar um atendimento já cancelado devolve **200**.
- **`atualizadoEm`** vem nas respostas de paciente e de atendimento, com a data da última alteração no servidor.
- **Os códigos de erro dizem o que o app deve fazer:**

  | Código | Significado | O app deve |
  |---|---|---|
  | 400 | Dados inválidos ou ação não permitida | parar de reenviar e avisar o usuário |
  | 401 | Login inválido | pedir login de novo |
  | 404 | Registro não encontrado | parar de reenviar e avisar o usuário |
  | 409 | Conflito (ex.: CPF já cadastrado) | parar de reenviar e avisar o usuário |
  | 500 ou sem resposta | Problema no servidor ou na rede | tentar de novo depois |

Os erros seguem o formato *Problem Details* (RFC 9457): a mensagem para o usuário está no campo `detail`, e os erros de validação ficam no campo `erros`, um por campo.

## Deploy (Render)

O projeto tem um `Dockerfile` em `spring-boot-cuidarme/`.

1. Crie uma conta em [render.com](https://render.com) e clique em **New → Web Service**.
2. Conecte este repositório do GitHub.
3. Configure:
   - **Root Directory:** `spring-boot-cuidarme`
   - **Runtime / Language:** Docker
   - **Instance type:** Free
4. Em **Environment**, adicione `DB_URL`, `DB_USERNAME` e `DB_PASSWORD`.
5. Clique em **Deploy**. A API fica em `https://<nome-do-servico>.onrender.com/cuidarme/api/`.

No plano gratuito, o servidor "dorme" depois de 15 minutos sem uso, e a primeira requisição depois disso demora cerca de 1 minuto. Antes de uma apresentação, abra o Swagger para acordá-lo.

Para testar a imagem localmente:

```bash
cd spring-boot-cuidarme
docker build -t cuidarme-api .
docker run --env-file .env -p 8082:8082 cuidarme-api
```
