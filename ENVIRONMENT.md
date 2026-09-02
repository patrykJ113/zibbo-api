# 🔐 Environment variables & secrets

This project keeps its secrets — database password, host, port and so on — in a
`.env` file in the project root.

That file **is committed to the repo**, and that is intentional. Every value in
it is **encrypted** with [Dotenvx](https://dotenvx.com), so what actually lands
in git looks like this:

```
DB_PASSWORD="encrypted:BFCG5PLxfEUp0P6QSMlB1+xDgssMcYSjKwLUgCVvsvj/PCtvi7jCGpZ1..."
```

Without the private key, that blob is useless. The private key lives in
**`.env.keys`**, which is **gitignored and never committed**.

> **The one rule:** `.env` goes in the repo, `.env.keys` never does.

---

## 1. Install Dotenvx

```bash
npm install -g @dotenvx/dotenvx
```

Check it worked:

```bash
dotenvx --version
```

---

## 2. One-time setup after cloning

Two things, once per machine.

### a) Get the private key

Ask the project owner for the **`.env.keys`** file and put it in the project
root, next to `pom.xml`. Without it you cannot decrypt anything, so the app
will not start.

**Why it's not in the repo:** it's the one secret that must stay secret. If it
leaked, every encrypted value in `.env` could be read by anyone with the repo.

### b) Install the pre-commit hook

```bash
dotenvx ext precommit --install
```

This writes a hook into `.git/hooks/pre-commit` that blocks any commit
containing a plaintext secret.

**Why you have to run it yourself:** git hooks are **not** copied when you
clone a repo. They live in `.git/hooks/`, which git does not track. So every
person on the project runs this once on their own machine.

---

## 3. Daily use

### Add or change a secret

```bash
dotenvx set DB_PASSWORD yourpassword
```

It's encrypted **immediately** as it's written — there's no window where the
plaintext sits in the file.

### Read a value back

```bash
dotenvx get DB_PASSWORD
```

Prints the decrypted value. Needs `.env.keys`.

### Run the app with secrets injected

```bash
dotenvx run -- mvn spring-boot:run
```

Dotenvx decrypts the values in memory and hands them to the process. Nothing is
written to disk in plaintext.

**From IntelliJ:** tick **Enable Dotenvx** in the run configuration and the IDE
does the same thing — no terminal needed.

---

## 4. What happens when you commit

Every `git commit` runs the hook automatically:

- ✅ **All values encrypted** → the commit goes through.
- ❌ **A plaintext secret found** → the commit is **blocked** before anything is
  recorded.

You can run the same check by hand at any time:

```bash
dotenvx ext precommit
```

A healthy project prints something like:

```
▣ encrypted/gitignored (2)
```

### If you get blocked

Encrypt, re-stage, commit again:

```bash
dotenvx encrypt
git add .env
git commit -m "your message"
```

---

## 5. How the encryption works

You don't need this to use the project — it's here so the `encrypted:` blobs
aren't a mystery.

**Two keys, different jobs:**

| Key | Lives in | Can do |
|---|---|---|
| **Public key** (`DOTENV_PUBLIC_KEY`) | `.env`, in the repo | **encrypt** only |
| **Private key** | `.env.keys`, gitignored | **decrypt** |

Because encrypting only needs the public key, anyone who clones the repo can
*add* a new secret without ever having the private key. Reading one requires
`.env.keys`.

**The scheme** is ECIES — elliptic-curve key exchange on **secp256k1** (the
same curve Bitcoin uses) combined with **AES-256-GCM** for the actual
encryption. Each value gets a freshly generated ephemeral key, which is why:

> Encrypting the same password twice produces two completely different blobs.
> That's expected — it's not a bug, and both decrypt to the same value.

**Quote styles** are preserved by `dotenvx encrypt`, so all of these are valid
and equivalent:

```
A="encrypted:..."
B='encrypted:...'
C=encrypted:...
```

`dotenvx set` always writes double quotes.

---

## Troubleshooting

| Symptom | Cause | Fix |
|---|---|---|
| App won't start, missing values | No `.env.keys` | Get it from the project owner |
| `command not found: dotenvx` | Not installed | `npm install -g @dotenvx/dotenvx` |
| Commit blocked | A plaintext value in `.env` | `dotenvx encrypt`, then re-add and commit |
| Commit **not** blocked by a bad `.env` | Hook never installed | `dotenvx ext precommit --install` |
| Commit works in terminal but fails in IntelliJ with `☠ 'dotenvx precommit' command not found` | See below | See below |

### `command not found` when committing from an IDE

If you installed node with **nvm**, `dotenvx` lives somewhere like
`~/.nvm/versions/node/v23.5.0/bin/`. nvm adds that to your `PATH` from
`~/.bashrc`, which only runs for **interactive terminal shells**.

IntelliJ, GitKraken and friends are launched from the desktop and never read
`~/.bashrc`, so the hook can't see `dotenvx` — even though it works perfectly
in your terminal.

The hook in this repo has a small `PATH` fix at the top that finds nvm's node
directories automatically. Two things to know:

- `.git/hooks/` is **not** cloned, so each person hits this on their own machine.
- Re-running `dotenvx ext precommit --install` **regenerates the hook and
  removes the fix**. Re-apply it if you reinstall.

A more permanent alternative — make `dotenvx` visible system-wide, once:

```bash
sudo ln -s "$(which dotenvx)" /usr/local/bin/dotenvx
```
