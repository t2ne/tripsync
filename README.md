
# ✈️ TripSync

**TripSync** é uma aplicação Android desenvolvida em Kotlin que permite aos utilizadores planear, organizar e sincronizar viagens de forma intuitiva e colaborativa. Inclui funcionalidades como criação de itinerários, partilha de planos, gestão de fotografias e suporte offline.

---

## 📖 Descrição do Projeto

O TripSync foi criado a pensar em viajantes que pretendem organizar facilmente várias viagens num único local. A aplicação permite:

- Criar e editar viagens com informações personalizadas
- Adicionar e visualizar fotografias associadas às viagens
- Partilhar planos com outros utilizadores
- Utilizar a aplicação em modo offline, com sincronização posterior
- Gerir várias viagens com uma interface simples e funcional

---

## 🧭 Estrutura do Projeto

```
TripSync
├── Activities
│   ├── LoginActivity
│   ├── RegisterActivity
│   ├── HomeActivity (Lista de Viagens)
│   ├── CriarViagemActivity
│   ├── EditarViagemActivity
│   ├── EditarPerfilActivity
│   └── FotosViagemActivity
├── Adapters
│   ├── ViagemAdapter
│   └── FotosAdapter
├── Models
│   ├── Viagem
│   └── FotoViagem
├── Utils
│   └── ImageUtils
└── Database
    ├── AppDatabase
    ├── ImageDao  
    └── ImageEntity
```

### 🔄 Fluxos de Dados

A comunicação entre as diferentes partes da aplicação segue uma arquitetura em camadas, facilitando a manutenção e separação de preocupações:

- **UI (Activities)**: Recebe input do utilizador e exibe os dados recebidos das fontes (Firebase/Room).
- **Adapters**: Fazem a ponte entre os dados e os componentes visuais como RecyclerView.
- **Models**: Estruturas que representam os dados usados localmente e remotamente.
- **Database (Room)**: Permite guardar imagens localmente e aceder a elas offline.
- **Firebase (Firestore/Storage/Auth)**: Fonte de dados remota para sincronização, autenticação e armazenamento na cloud.
- **Utils**: Suporte a operações repetitivas, como manipulação de imagens ou formatações.

### 🔁 Interações Entre Componentes

- **LoginActivity ↔ Firebase Auth**  
  Permite iniciar sessão e autenticar utilizadores. Em caso de sucesso, redireciona para a HomeActivity.

- **HomeActivity ↔ Firestore**  
  Recupera as viagens do utilizador autenticado e exibe-as usando o ViagemAdapter.

- **CriarViagemActivity / EditarViagemActivity ↔ Firestore + Room**  
  Permite criar ou modificar viagens. As imagens associadas podem ser guardadas localmente (Room) e na cloud (Storage).

- **FotosViagemActivity ↔ Room + Firebase Storage**
  Mostra as imagens guardadas localmente, sincroniza com a cloud e permite adicionar novas fotos a partir da galeria.

- **Offline:**
  Quando o utilizador está offline, as imagens são guardadas no Room e sincronizadas com o Firestore/Storage assim que houver ligação à internet.

---

## 📦 Dependências e Tecnologias

Este projeto utiliza as seguintes bibliotecas:

- **Firebase Authentication** – Autenticação de utilizadores
- **Firebase Firestore** – Armazenamento remoto de dados
- **Firebase Storage** – Armazenamento de imagens na cloud
- **Firebase Crashlytics** – Relatórios de falhas
- **Room Database** – Armazenamento local de imagens
- **ViewBinding** – Acesso seguro às views
- **Material Components** – Interface moderna e consistente
- **RecyclerView & CardView** – Listagem eficiente de dados
- **Jetpack Libraries** – AppCompat, Core, Activity, ConstraintLayout
- **Kotlin Coroutines** – Assíncrono e reativo
- **MockK** – Testes unitários e de instrumentação

---

## 🔧 Configuração e Instalação

### Requisitos

- Android Studio Electric Eel ou superior
- SDK Mínimo: Android 7.1 (API 25)
- SDK Alvo: Android 14 (API 35)
- Kotlin 1.9.0 ou superior
- Java 11

### Passos para Compilar

```bash
git clone https://github.com/t2ne/tripsync.git
```

1. Abrir o Android Studio
2. Clonar projeto dum repositório remoto
3. Colar o link https://github.com/t2ne/tripsync.git
4. Criar o projeto
5. Esperar pela sincronização do Gradle
6. Ligar um dispositivo ou emulador
7. Executar a aplicação

---

## 📱 Instruções de Utilização

1. Iniciar sessão ou criar conta
2. Criar uma nova viagem com título, descrição e datas
3. Associar locais à viagem
4. Adicionar fotografias diretamente da galeria
5. Editar ou apagar viagens conforme necessário
6. Partilhar planos com outros utilizadores

---

## 📋 Requisitos Funcionais

- Criar, editar e eliminar viagens
- Adicionar e remover fotografias associadas a viagens
- Registar e autenticar utilizadores com a Firebase
- Sincronizar dados com o Firestore (online e offline)
- Guardar imagens localmente com o Room
- Partilhar viagens com outros utilizadores

---

## ❗ Requisitos Não Funcionais

- A aplicação deve funcionar offline com sincronização automática quando estiver online
- Interface intuitiva e responsiva
- Compatibilidade com dispositivos Android API 25+
- Performance fluída sem atrasos visíveis
- Segurança garantida com autenticação via Firebase
- Código modular e de fácil manutenção

---

## 🔐 Permissões Utilizadas

A aplicação requer as seguintes permissões, definidas no `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />

<uses-permission
    android:name="android.permission.READ_EXTERNAL_STORAGE"
    android:maxSdkVersion="32" />

<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
```

Estas permissões são utilizadas para:

- Acesso à internet (Firebase)
- Acesso à galeria de imagens (dependente da versão do Android)

---

## 👥 Autores

- [t2ne](https://github.com/t2ne)
- [cyzuko](https://github.com/cyzuko)
