package com.ipiecoles.java.java230;

import com.ipiecoles.java.java230.exceptions.BatchException;
import com.ipiecoles.java.java230.exceptions.TechnicienException;
import com.ipiecoles.java.java230.model.Commercial;
import com.ipiecoles.java.java230.model.Employe;
import com.ipiecoles.java.java230.model.Manager;
import com.ipiecoles.java.java230.model.Technicien;
import com.ipiecoles.java.java230.repository.CommercialRepository;
import com.ipiecoles.java.java230.repository.EmployeRepository;
import com.ipiecoles.java.java230.repository.ManagerRepository;
import com.ipiecoles.java.java230.repository.TechnicienRepository;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class MyRunner implements CommandLineRunner {

    private static final String REGEX_MATRICULE = "^[MTC][0-9]{5}$";
    private static final String REGEX_NOM = ".*";
    private static final String REGEX_PRENOM = ".*";
    private static final int NB_CHAMPS_MANAGER = 5;
    private static final int NB_CHAMPS_TECHNICIEN = 7;
    private static final String REGEX_MATRICULE_MANAGER = "^M[0-9]{5}$";
    private static final int NB_CHAMPS_COMMERCIAL = 7;

    @Autowired
    private EmployeRepository employeRepository;

    @Autowired
    private ManagerRepository managerRepository;

    @Autowired
    private CommercialRepository commercialRepository;

    @Autowired
    private TechnicienRepository technicienRepository;

    private List<Employe> employes = new ArrayList<>();

    // Le logger est très important !

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void run(String... strings) throws Exception {
        String fileName = "employes.csv";
        readFile(fileName);
        //readFile(strings[0]);
    }

    /**
     * Méthode qui lit le fichier CSV en paramètre afin d'intégrer son contenu en BDD
     * @param fileName Le nom du fichier (à mettre dans src/main/resources)
     * @return une liste contenant les employés à insérer en BDD ou null si le fichier n'a pas pu être le
     */
    public List<Employe> readFile(String fileName) {
        Stream<String> stream = null;
        // Catcher l'exception en cas de fichier non trouvé
        try {
            stream = Files.lines(Paths.get(new ClassPathResource(fileName).getURI()));
        }
        catch(IOException e){
            logger.error("Problème dans l'ouverture du fichier " + fileName);
            return null;
        }
        //Afficher chaque ligne du fichier dans la console
        List<String> lignes = stream.collect(Collectors.toList());
        for(int i = 0; i < lignes.size(); i++){
            System.out.println((lignes.get(i)));
            try {
                processLine(lignes.get(i)); // Regarde la première lettre de la ligne
            } catch (BatchException e) {
                logger.error("Ligne " + (i + 1) + " : " + e.getMessage() + " => " + lignes.get(i));
                // On passe à la ligne suivante
            }

        }

        /*logger.error("Ceci est une erreur");
        logger.warn("Ceci est un avertissement");
        logger.info("Ceci est une info");*/

        return employes;
    }

    /**
     * Méthode qui regarde le premier caractère de la ligne et appelle la bonne méthode de création d'employé
     * @param ligne la ligne à analyser
     * @throws BatchException si le type d'employé n'a pas été reconnu
     */
    private void processLine(String ligne) throws BatchException {


        switch(ligne.substring(0,1)){
            case "T":
                processTechnicien(ligne);
                break;
            case "M":
                processManager(ligne);
                break;
            case "C":
                processCommercial(ligne);
                break;
            default:
                throw new BatchException("Type d'employé inconnu : " + ligne.substring(0, 1) );
        }

    }

    /**
     * Méthode qui crée un Commercial à partir d'une ligne contenant les informations d'un commercial et l'ajoute dans la liste globale des employés
     * @param ligneCommercial la ligne contenant les infos du commercial à intégrer
     * @throws BatchException s'il y a un problème sur cette ligne
     */
    private void processCommercial(String ligneCommercial) throws BatchException {

        // Découpage de la ligne avec la virgule
        String[] champs = ligneCommercial.split(",");

        // Vérification du nombre de champs
        verifChamps(champs.length, NB_CHAMPS_COMMERCIAL);

        // Vérification du format du matricule
        String matricule = verifMat(champs[0]);

        // Vérification du format de la date
        LocalDate embauche = verifDate(champs[3]);

        // Vérification du format du salaire
        Double salaire = verifSalaire(champs[4]);

        // Vérification du format du chiffre d'affaire
        Double chiffreAffaire = verifChiffreAffaire(champs[5]);

        // Vérification du format de la performance
        Integer performance = verifPerformance(champs[6]);

        // Création du commercial et ajout dans le repository
        commercialRepository.save(new Commercial(champs[1], champs[2], matricule, embauche, salaire, chiffreAffaire, performance));
    }

    /**
     * Méthode qui crée un Manager à partir d'une ligne contenant les informations d'un manager et l'ajoute dans la liste globale des employés
     * @param ligneManager la ligne contenant les infos du manager à intégrer
     * @throws BatchException s'il y a un problème sur cette ligne
     */
    private void processManager(String ligneManager) throws BatchException {

        // Découpage de la ligne avec la virgule
        String[] champs = ligneManager.split(",");

        // Vérification du nombre de champs
        verifChamps(champs.length, NB_CHAMPS_MANAGER);

        // Vérification du format du matricule
        String matricule = verifMat(champs[0]);

        // Vérification du format de la date
        LocalDate embauche = verifDate(champs[3]);

        // Vérification du format du salaire
        Double salaire = verifSalaire(champs[4]);

        // Création du manager et ajout dans le repository
        managerRepository.save(new Manager(champs[1], champs[2], matricule, embauche, salaire));

    }

    /**
     * Méthode qui crée un Technicien à partir d'une ligne contenant les informations d'un technicien et l'ajoute dans la liste globale des employés
     * @param ligneTechnicien la ligne contenant les infos du technicien à intégrer
     * @throws BatchException s'il y a un problème sur cette ligne
     */
    private void processTechnicien(String ligneTechnicien) throws BatchException {

        // Découpage de la ligne avec la virgule
        String[] champs = ligneTechnicien.split(",");

        // Vérification du nombre de champs
        verifChamps(champs.length, NB_CHAMPS_TECHNICIEN);

        // Vérification du format du matricule
        String matricule = verifMat(champs[0]);

        // Vérification du format de la date
        LocalDate embauche = verifDate(champs[3]);

        // Vérification du format du salaire
        Double salaire = verifSalaire(champs[4]);

        // Vérification du format du grade
        Integer grade = verifGrade(champs[5]);

        // Vérification du format du matricule du manager
        String matManager = verifMat(champs[6]);

        // Vérification de l'existence du manager et récupération de celui-ci
        Manager manager = verifManager(matManager);

        // Création du technicien et ajout dans le repository
        try {
            Technicien zero = new Technicien(champs[1], champs[2], matricule, embauche, salaire, grade);
            zero.setManager(manager);
            technicienRepository.save(zero);
        } catch (TechnicienException te) {
            throw new BatchException(te.getMessage());
        }

    }


    private LocalDate verifDate(String date) throws BatchException {
        try {
            return DateTimeFormat.forPattern("dd/MM/yyyy").parseLocalDate(date);
        } catch (Exception e) {
            throw new BatchException(date + " ne respecte pas le format de date dd/MM/yyyy");
        }
    }

    private double verifSalaire(String salaire) throws BatchException {
        try {
            return Double.parseDouble(salaire);
        } catch (Exception e) {
            throw new BatchException(salaire + " n'est pas un nombre valide pour un salaire");
        }
    }

    private String verifMat(String matricule) throws BatchException {

        if (matricule.matches(REGEX_MATRICULE)){
            return matricule;
        } else  {
            throw new BatchException("La chaîne " + matricule + " ne respecte pas l'expression régulière ^[MTC][0-9]{5}$");
        }
    }

    private void verifChamps(int tailleChamps, int tailleNecessaire) throws BatchException {
        if (tailleChamps != tailleNecessaire) {
            throw new BatchException("La ligne manager ne contient pas " + tailleNecessaire + " éléments mais " + tailleChamps);
        }
    }

    private Double verifChiffreAffaire(String chiffreAffaire) throws BatchException {
        try {
            return Double.parseDouble(chiffreAffaire);
        }
        catch (Exception e) {
            throw new BatchException("Le chiffre d'affaire du commercial est incorrect : " + chiffreAffaire);
        }
    }

    private int verifPerformance(String performance) throws BatchException {
        try {
            return Integer.parseInt(performance);
        }
        catch (Exception e) {
            throw new BatchException("La performance du commercial est incorrecte : " + performance);
        }
    }

    private int verifGrade(String grade) throws BatchException {
        try {
            return Integer.parseInt(grade);
        }
        catch (Exception e) {
            throw new BatchException("Le grade du technicien est incorrect : " + grade);
        }
    }

    private Manager verifManager(String matricule) throws BatchException {

        Manager blop = managerRepository.findByMatricule(matricule);

        if (blop == null) {
            throw new BatchException("Le manager de matricule " + matricule + " n'a pas été trouvé dans le fichier ou en base de données");
        }
        else {
            return blop;
        }


    }

}
