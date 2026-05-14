/**
 * Types partages entre tous les domaines metier du frontend.
 *
 * Contenu :
 *  - apiError       : ApiError + ApiFieldErrors (format d'erreur unifie du back)
 *  - enums          : Role (ADHERENT, BIBLIOTHECAIRE, ADMINISTRATEUR)
 *  - langue         : Langue (FR, EN, ES)
 *  - statutEmprunt  : StatutEmprunt (EN_COURS, RETOURNE)
 *  - statutUtilisateur : StatutUtilisateur (ACTIF, INACTIF, ANONYMISE)
 *
 * Tous ces types sont des miroirs TypeScript stricts des enums et records
 * Java backend (com.chague.bibliotheque.domain.*). La synchronisation est
 * garantie a la compilation : une saisie invalide cote front est rejetee
 * par le compilateur TypeScript, en miroir de la validation Jakarta
 * @NotNull et de la deserialisation Jackson cote serveur.
 */
export * from "~/types/shared/apiError";
export * from "~/types/shared/enums";
export * from "~/types/shared/langue";
export * from "~/types/shared/statutEmprunt";
export * from "~/types/shared/statutUtilisateur";
