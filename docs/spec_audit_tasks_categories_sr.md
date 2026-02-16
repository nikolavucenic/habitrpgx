# Audit implementacije specifikacije (sekcije 2 i 3)

## Opseg pregleda
Ovaj dokument proverava implementaciju zahteva iz specifikacije:
- 2.1 Kreiranje ponavljajućih/jednokratnih zadataka
- 2.2 Pregled svih zadataka
- 2.3 Izmena i brisanje zadataka
- 2.4 Rešavanje zadataka
- 3. Upravljanje kategorijama

Takođe proverava usklađenost sa Clean Architecture + MVI.

## Kratki zaključak
- **Delimično implementirano**: kreiranje zadatka, osnovni prikaz liste i kalendara, promene statusa, osnovna kreacija kategorije.
- **Nedostaje više ključnih stavki iz specifikacije**: detaljan prikaz zadatka, izmena/brisanje kroz UI, kompletno upravljanje kategorijama, kvote za XP i kompletna pravila ponavljanja.
- **Arhitektura je delimično čista**: postoji slojevitost `app/domain/data`, ali je business logika previše rasuta između UI i repository sloja, a MVI je implementiran samo delimično.

---

## 2.1 Kreiranje ponavljajućih ili jednokratnih zadataka

### Pokriveno
- Korisnik može kreirati zadatak sa tipom `ONE_TIME` ili `REPEATING`.
- Pri kreiranju postoje polja za: kategoriju, učestalost, težinu, bitnost, naziv, opcioni opis i vreme izvršenja.
- Za ponavljajući zadatak postoje: interval, jedinica (`DAY/WEEK`), početni i završni datum.
- XP vrednost zadatka računa se kao zbir težine i bitnosti.

### Nepokriveno / odstupanja
- Ne postoji validacija da je interval > 0 niti jače validacije raspona datuma za ponavljanje.
- Ne postoji logika koja kreira buduće instance ponavljajućih zadataka; čuva se jedan zapis sa repeat metapodacima.
- Nisu implementirane kvote bodovanja po težini/bitnosti (dnevno/nedeljno/mesečno ograničenje).

## 2.2 Pregled svih zadataka

### Pokriveno
- Postoji lista zadataka sa tab filterima: svi / jednokratni / ponavljajući.
- Postoji kalendarski prikaz gde se zadaci filtriraju po odabranom datumu.
- U listi i kalendaru moguće je menjati status (urađeno, otkazano, pauzirano, aktivno).
- Boja kategorije je prikazana po zadatku (dot indicator).

### Nepokriveno / odstupanja
- Ne postoji „detaljan pregled zadatka” klikom na zadatak.
- U listi nisu filtrirani samo trenutni i budući zadaci; prikazuju se svi koji dođu iz repozitorijuma.
- U kalendaru ne postoji vremenski raspored/slot prikaz (samo lista za izabrani dan).

## 2.3 Izmena i brisanje zadataka

### Pokriveno
- U repository sloju postoje `updateTask` i `deleteTask` sa osnovnim zaštitama za završene/neaktivne statuse.

### Nepokriveno / odstupanja
- Ne postoji UI stranica za izmenu/brisanje zadatka.
- Ne postoji akcija/use-case wiring u app sloju za update/delete task kroz MVI tok.
- Za ponavljajući zadatak nije implementirana semantika „menjaj samo buduća ponavljanja”, jer ni ponavljanja nisu modelovana kao zasebne instance.
- Pravilo „nije moguće obrisati završene zadatke” jeste delimično pokriveno, ali kroz internu proveru statusa u repository-ju, bez UX toka koji to transparentno vodi korisnika.

## 2.4 Rešavanje zadataka

### Pokriveno
- Statusi postoje: `ACTIVE`, `DONE`, `NOT_DONE`, `PAUSED`, `CANCELED`.
- Kreiran zadatak startuje kao `ACTIVE`.
- `DONE` je dozvoljen samo kada je vreme izvršenja prošlo.
- Posle 3 dana od `executeAt`, pokušaj `DONE` prebacuje zadatak u `NOT_DONE`.
- Pauziranje je ograničeno na ponavljajuće zadatke.
- Kod `DONE`, XP se upisuje korisniku i sabira sa postojećim XP.
- Otkazan/pauziran ne dodeljuje XP (pošto se XP dodeljuje samo kod prelaska na `DONE`).

### Nepokriveno / odstupanja
- Automatski prelaz u `NOT_DONE` nije stvarno automatski u pozadini; dešava se tek kada korisnik pokuša `DONE` nakon roka.
- Nedostaje jasna tranziciona matrica statusa u domenskom sloju (pravila su u repository implementaciji).
- Nema eksplicitne zaštite od ponovnog dodeljivanja XP-a pri potencijalno višestrukim izmenama statusa ka `DONE` (oslanja se na trenutno stanje zapisa).
- Nema implementacije kvota XP bodovanja iz specifikacije.

## 3. Upravljanje kategorijama

### Pokriveno
- Kategorije mogu da se kreiraju.
- Kategorija ima naziv i boju.
- Jedinstvenost boje se proverava pri kreiranju i izmeni kategorije.
- Brisanje kategorije je blokirano ako postoje aktivni zadaci te kategorije.

### Nepokriveno / odstupanja
- Ne postoji zasebna stranica za pregled/upravljanje kategorijama.
- Ne postoji UI za izmenu boje/naziva kategorije.
- Ne postoji UI za brisanje kategorije.
- Nije implementiran UX tok koji jasno pokazuje posledice promene boje nad svim zadacima.

---

## Clean Architecture ocena

### Pozitivno
- Jasna podela modula: `domain`, `data`, `app`.
- `domain` definiše modele, repozitorijumske interfejse i use-case klase.
- `data` sadrži implementacije repozitorijuma.
- `app` sloj koristi ViewModel + akcije/stanje/sporedne efekte.

### Problemi
- Značajan deo poslovnih pravila je u `TaskRepositoryImpl` (data sloj), umesto da bude u use-case / domain servisima.
- `CreateTaskFragment` izračunava XP i mapira težinu/bitnost (UI drži business pravila).
- Nedostaju use-case klase za update/delete task i update/delete category (iako repository interfejs to ima).
- Domain modeli koriste string konstante za status/težinu/bitnost bez tipizovanih enum/value objekata, što otežava robustnost pravila.

## MVI ocena

### Pozitivno
- Postoje `Action`, `UiState`, `SideEffect` klase i centralni `handleAction` u ViewModel-u.
- UI posmatra stanje i side-effect tokove.

### Problemi
- Efekti i stanja nisu uvek striktno jednosmerni (npr. `Load` se trigguje iz više fragmenata preko shared ViewModel-a, što komplikuje izvor istine).
- Nedostaje detaljnija granularnost state modela za kompleksna pravila (npr. task detalj, edit flow, category management flow).
- Deo validacije/transformacije podataka je u Fragment-u umesto u ViewModel/domain sloju.

---

## Preporučeni prioriteti (redom)
1. **Implementirati task details + edit/delete flow** (UI + Action + UseCase + Repository wiring).
2. **Prebaciti business pravila iz Fragment-a/repository-ja u domain use-case sloj** (XP računanje, status tranzicije, pravila ponavljanja).
3. **Implementirati category management ekran** (list/update/delete) sa pravilima jedinstvene boje.
4. **Implementirati XP kvote** po periodima i kombinacijama težina/bitnost.
5. **Modelovati ponavljanja kao instance ili eksplicitan scheduler pristup** da bi kalendar/lista i „samo buduća ponavljanja” bili ispravno podržani.
