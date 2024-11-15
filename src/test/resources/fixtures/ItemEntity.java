package fixtures;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "items")
public class ItemEntity {
    @Id
    private String id;

    @ManyToMany
    @JoinColumn(nullable = false, name = "inventory_id")
    private List<ch.nmeylan.plugin.jpa.generator.fixtures.InventoryEntity> inventories;

    @Column(name = "name_aegis")
    private String nameAegis;

    @Column(name = "name_english")
    private String nameEnglish;

    @Column(name = "type")
    private String type;

    @Column(name = "sub_type")
    private String subType;

    @Column(name = "price_buy")
    private BigDecimal priceBuy;

    @Column(name = "price_sell")
    private BigDecimal priceSell;

    @Column(name = "weight")
    private Integer weight;

    @Column(name = "defense")
    private Integer defense;

    @Column(name = "attack")
    private Integer attack;

    @Column(name = "range")
    private Integer range;

    @Column(name = "slots")
    private Integer slots;

    @Column(name = "job_all")
    private Boolean jobAll;

    @Column(name = "job_acolyte")
    private Boolean jobAcolyte;

    @Column(name = "job_archer")
    private Boolean jobArcher;

    @Column(name = "job_alchemist")
    private Boolean jobAlchemist;

    @Column(name = "job_assassin")
    private Boolean jobAssassin;

    @Column(name = "job_barddancer")
    private Boolean jobBarddancer;
    @Column(name = "job_blacksmith")
    private Boolean jobBlacksmith;
    @Column(name = "job_crusader")
    private Boolean jobCrusader;
    @Column(name = "job_gunslinger")
    private Boolean jobGunslinger;
    @Column(name = "job_hunter")
    private Boolean jobHunter;
    @Column(name = "job_knight")
    private Boolean jobKnight;
    @Column(name = "job_mage")
    private Boolean jobMage;
    @Column(name = "job_merchant")
    private Boolean jobMerchant;
    @Column(name = "job_monk")
    private Boolean jobMonk;
    @Column(name = "job_ninja")
    private Boolean jobNinja;
    @Column(name = "job_novice")
    private Boolean jobNovice;
    @Column(name = "job_priest")
    private Boolean jobPriest;
    @Column(name = "job_rogue")
    private Boolean jobRogue;
    @Column(name = "job_sage")
    private Boolean jobSage;
    @Column(name = "job_soullinker")
    private Boolean jobSoullinker;
    @Column(name = "job_stargladiator")
    private Boolean jobStargladiator;
    @Column(name = "job_supernovice")
    private Boolean jobSupernovice;
    @Column(name = "job_swordman")
    private Boolean jobSwordman;
    @Column(name = "job_taekwon")
    private Boolean jobTaekwon;
    @Column(name = "job_thief")
    private Boolean jobThief;
    @Column(name = "job_wizard")
    private Boolean jobWizard;


    public String getId() {
        return id;
    }

    public ItemEntity setId(String id) {
        this.id = id;
        return this;
    }

    public String getNameAegis() {
        return nameAegis;
    }

    public ItemEntity setNameAegis(String nameAegis) {
        this.nameAegis = nameAegis;
        return this;
    }

    public String getNameEnglish() {
        return nameEnglish;
    }

    public ItemEntity setNameEnglish(String nameEnglish) {
        this.nameEnglish = nameEnglish;
        return this;
    }

    public String getType() {
        return type;
    }

    public ItemEntity setType(String type) {
        this.type = type;
        return this;
    }

    public String getSubType() {
        return subType;
    }

    public ItemEntity setSubType(String subType) {
        this.subType = subType;
        return this;
    }

    public Integer getPriceBuy() {
        if (priceBuy == null) {
            return null;
        }
        return priceBuy.toBigInteger().intValue();
    }

    public ItemEntity setPriceBuy(Integer priceBuy) {
        if (priceBuy != null) {
            this.priceBuy = new BigDecimal(priceBuy);
        }
        return this;
    }

    public Integer getPriceSell() {
        if (priceSell == null) {
            return null;
        }
        return priceSell.toBigInteger().intValue();
    }

    public ItemEntity setPriceSell(Integer priceSell) {
        if (priceSell != null) {
            this.priceSell =  new BigDecimal(priceSell);
        }
        return this;
    }

    public Integer getWeight() {
        return weight;
    }

    public ItemEntity setWeight(Integer weight) {
        this.weight = weight;
        return this;
    }

    public Integer getDefense() {
        return defense;
    }

    public ItemEntity setDefense(Integer defense) {
        this.defense = defense;
        return this;
    }

    public Integer getAttack() {
        return attack;
    }

    public ItemEntity setAttack(Integer attack) {
        this.attack = attack;
        return this;
    }

    public Integer getRange() {
        return range;
    }

    public ItemEntity setRange(Integer range) {
        this.range = range;
        return this;
    }

    public Integer getSlots() {
        return slots;
    }

    public ItemEntity setSlots(Integer slots) {
        this.slots = slots;
        return this;
    }

    public Boolean getJobAll() {
        return jobAll;
    }

    public ItemEntity setJobAll(Boolean jobAll) {
        this.jobAll = jobAll;
        return this;
    }

    public Boolean getJobAcolyte() {
        return jobAcolyte;
    }

    public ItemEntity setJobAcolyte(Boolean jobAcolyte) {
        this.jobAcolyte = jobAcolyte;
        return this;
    }

    public Boolean getJobArcher() {
        return jobArcher;
    }

    public ItemEntity setJobArcher(Boolean jobArcher) {
        this.jobArcher = jobArcher;
        return this;
    }

    public Boolean getJobAlchemist() {
        return jobAlchemist;
    }

    public ItemEntity setJobAlchemist(Boolean jobAlchemist) {
        this.jobAlchemist = jobAlchemist;
        return this;
    }

    public Boolean getJobAssassin() {
        return jobAssassin;
    }

    public ItemEntity setJobAssassin(Boolean jobAssassin) {
        this.jobAssassin = jobAssassin;
        return this;
    }

    public Boolean getJobBarddancer() {
        return jobBarddancer;
    }

    public ItemEntity setJobBarddancer(Boolean jobBarddancer) {
        this.jobBarddancer = jobBarddancer;
        return this;
    }

    public Boolean getJobBlacksmith() {
        return jobBlacksmith;
    }

    public ItemEntity setJobBlacksmith(Boolean jobBlacksmith) {
        this.jobBlacksmith = jobBlacksmith;
        return this;
    }

    public Boolean getJobCrusader() {
        return jobCrusader;
    }

    public ItemEntity setJobCrusader(Boolean jobCrusader) {
        this.jobCrusader = jobCrusader;
        return this;
    }

    public Boolean getJobGunslinger() {
        return jobGunslinger;
    }

    public ItemEntity setJobGunslinger(Boolean jobGunslinger) {
        this.jobGunslinger = jobGunslinger;
        return this;
    }

    public Boolean getJobHunter() {
        return jobHunter;
    }

    public ItemEntity setJobHunter(Boolean jobHunter) {
        this.jobHunter = jobHunter;
        return this;
    }

    public Boolean getJobKnight() {
        return jobKnight;
    }

    public ItemEntity setJobKnight(Boolean jobKnight) {
        this.jobKnight = jobKnight;
        return this;
    }

    public Boolean getJobMage() {
        return jobMage;
    }

    public ItemEntity setJobMage(Boolean jobMage) {
        this.jobMage = jobMage;
        return this;
    }

    public Boolean getJobMerchant() {
        return jobMerchant;
    }

    public ItemEntity setJobMerchant(Boolean jobMerchant) {
        this.jobMerchant = jobMerchant;
        return this;
    }

    public Boolean getJobMonk() {
        return jobMonk;
    }

    public ItemEntity setJobMonk(Boolean jobMonk) {
        this.jobMonk = jobMonk;
        return this;
    }

    public Boolean getJobNinja() {
        return jobNinja;
    }

    public ItemEntity setJobNinja(Boolean jobNinja) {
        this.jobNinja = jobNinja;
        return this;
    }

    public Boolean getJobNovice() {
        return jobNovice;
    }

    public ItemEntity setJobNovice(Boolean jobNovice) {
        this.jobNovice = jobNovice;
        return this;
    }

    public Boolean getJobPriest() {
        return jobPriest;
    }

    public ItemEntity setJobPriest(Boolean jobPriest) {
        this.jobPriest = jobPriest;
        return this;
    }

    public Boolean getJobRogue() {
        return jobRogue;
    }

    public ItemEntity setJobRogue(Boolean jobRogue) {
        this.jobRogue = jobRogue;
        return this;
    }

    public Boolean getJobSage() {
        return jobSage;
    }

    public ItemEntity setJobSage(Boolean jobSage) {
        this.jobSage = jobSage;
        return this;
    }

    public Boolean getJobSoullinker() {
        return jobSoullinker;
    }

    public ItemEntity setJobSoullinker(Boolean jobSoullinker) {
        this.jobSoullinker = jobSoullinker;
        return this;
    }

    public Boolean getJobStargladiator() {
        return jobStargladiator;
    }

    public ItemEntity setJobStargladiator(Boolean jobStargladiator) {
        this.jobStargladiator = jobStargladiator;
        return this;
    }

    public Boolean getJobSupernovice() {
        return jobSupernovice;
    }

    public ItemEntity setJobSupernovice(Boolean jobSupernovice) {
        this.jobSupernovice = jobSupernovice;
        return this;
    }

    public Boolean getJobSwordman() {
        return jobSwordman;
    }

    public ItemEntity setJobSwordman(Boolean jobSwordman) {
        this.jobSwordman = jobSwordman;
        return this;
    }

    public Boolean getJobTaekwon() {
        return jobTaekwon;
    }

    public ItemEntity setJobTaekwon(Boolean jobTaekwon) {
        this.jobTaekwon = jobTaekwon;
        return this;
    }

    public Boolean getJobThief() {
        return jobThief;
    }

    public ItemEntity setJobThief(Boolean jobThief) {
        this.jobThief = jobThief;
        return this;
    }

    public Boolean getJobWizard() {
        return jobWizard;
    }

    public ItemEntity setJobWizard(Boolean jobWizard) {
        this.jobWizard = jobWizard;
        return this;
    }
}
