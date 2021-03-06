package emulation.i8080.cpu;

enum Instruction {
    NOP,    LXI,   STAX,    INX,
    INR,    DCR,    MVI,    RLC,
    DAD,   LDAX,    DCX,    RRC,
    SHLD,  LHLD,    STA,    LDA,
    RAL,    DAA,    STC,    RAR,
    CMA,    CMC,    MOV,    HLT,
    ADD,    ADC,    SUB,    SBB,
    ANA,    XRA,    ORA,    CMP,
    JMP,   CALL,    RET,    CPI,
    PUSH,  XCHG,    POP,    OUT,
    ADI,    SUI,    ANI,    ORI,
    DI,      EI,    PCHL,  SPHL,
    ACI,    SBI,    XRI,   XTHL,
    IN,     RST,
    UNK;

    public static final Instruction[] decoded = {
            NOP, LXI, STAX, INX, INR, DCR, MVI, RLC, NOP, DAD, LDAX, DCX, INR, DCR, MVI, RRC,
            NOP, LXI, STAX, INX, INR, DCR, MVI, RAL, NOP, DAD, LDAX, DCX, INR, DCR, MVI, RAR,
            NOP, LXI, SHLD, INX, INR, DCR, MVI, DAA, NOP, DAD, LHLD, DCX, INR, DCR, MVI, CMA,
            NOP, LXI,  STA, INX, INR, DCR, MVI, STC, NOP, DAD,  LDA, DCX, INR, DCR, MVI, CMC,

            MOV, MOV, MOV, MOV, MOV, MOV, MOV, MOV, MOV, MOV, MOV, MOV, MOV, MOV, MOV, MOV,
            MOV, MOV, MOV, MOV, MOV, MOV, MOV, MOV, MOV, MOV, MOV, MOV, MOV, MOV, MOV, MOV,
            MOV, MOV, MOV, MOV, MOV, MOV, MOV, MOV, MOV, MOV, MOV, MOV, MOV, MOV, MOV, MOV,
            MOV, MOV, MOV, MOV, MOV, MOV, HLT, MOV, MOV, MOV, MOV, MOV, MOV, MOV, MOV, MOV,

            ADD, ADD, ADD, ADD, ADD, ADD, ADD, ADD, ADC, ADC, ADC, ADC, ADC, ADC, ADC, ADC,
            SUB, SUB, SUB, SUB, SUB, SUB, SUB, SUB, SBB, SBB, SBB, SBB, SBB, SBB, SBB, SBB,
            ANA, ANA, ANA, ANA, ANA, ANA, ANA, ANA, XRA, XRA, XRA, XRA, XRA, XRA, XRA, XRA,
            ORA, ORA, ORA, ORA, ORA, ORA, ORA, ORA, CMP, CMP, CMP, CMP, CMP, CMP, CMP, CMP,

            RET, POP, JMP,  JMP, CALL, PUSH, ADI, RST, RET,  RET,  JMP,  UNK, CALL, CALL, ACI, RST,
            RET, POP, JMP,  OUT, CALL, PUSH, SUI, RST, RET,  UNK,  JMP,   IN, CALL,  UNK, SBI, RST,
            RET, POP, JMP, XTHL, CALL, PUSH, ANI, RST, RET, PCHL,  JMP, XCHG, CALL,  UNK, XRI, RST,
            RET, POP, JMP,   DI, CALL, PUSH, ORI, RST, RET, SPHL,  JMP,   EI, CALL,  UNK, CPI, RST,
    };
    
    public static String[] mnemonics = {
            "NOP",
            "LXI B,#D",
            "STAX B",
            "INX B",
            "INR B",
            "DCR B",
            "MVI B,#d",
            "RLC",
            "NOP",
            "DAD B",
            "LDAX B",
            "DCX B",
            "INR C",
            "DCR C",
            "MVI C,#d",
            "RRC",
            "NOP",
            "LXI D,#D",
            "STAX D",
            "INX D",
            "INR D",
            "DCR D",
            "MVI D,#d",
            "RAL",
            "NOP",
            "DAD D",
            "LDAX D",
            "DCX D",
            "INR E",
            "DCR E",
            "MVI E,#d",
            "RAR",
            "NOP",
            "LXI H,#D",
            "SHLD #A",
            "INX H",
            "INR H",
            "DCR H",
            "MVI H,#d",
            "DAA",
            "NOP",
            "DAD H",
            "LHLD #A",
            "DCX H",
            "INR L",
            "DCR L",
            "MVI L,#d",
            "CMA",
            "NOP",
            "LXI SP,#D",
            "STA #A",
            "INX SP",
            "INR M",
            "DCR M",
            "MVI M,#d",
            "STC",
            "NOP",
            "DAD SP",
            "LDA #A",
            "DCX SP",
            "INR A",
            "DCR A",
            "MVI A,#d",
            "CMC",
            "MOV B,B",
            "MOV B,C",
            "MOV B,D",
            "MOV B,E",
            "MOV B,H",
            "MOV B,L",
            "MOV B,M",
            "MOV B,A",
            "MOV C,B",
            "MOV C,C",
            "MOV C,D",
            "MOV C,E",
            "MOV C,H",
            "MOV C,L",
            "MOV C,M",
            "MOV C,A",
            "MOV D,B",
            "MOV D,C",
            "MOV D,D",
            "MOV D,E",
            "MOV D,H",
            "MOV D,L",
            "MOV D,M",
            "MOV D,A",
            "MOV E,B",
            "MOV E,C",
            "MOV E,D",
            "MOV E,E",
            "MOV E,H",
            "MOV E,L",
            "MOV E,M",
            "MOV E,A",
            "MOV H,B",
            "MOV H,C",
            "MOV H,D",
            "MOV H,E",
            "MOV H,H",
            "MOV H,L",
            "MOV H,M",
            "MOV H,A",
            "MOV L,B",
            "MOV L,C",
            "MOV L,D",
            "MOV L,E",
            "MOV L,H",
            "MOV L,L",
            "MOV L,M",
            "MOV L,A",
            "MOV M,B",
            "MOV M,C",
            "MOV M,D",
            "MOV M,E",
            "MOV M,H",
            "MOV M,L",
            "HLT",
            "MOV M,A",
            "MOV A,B",
            "MOV A,C",
            "MOV A,D",
            "MOV A,E",
            "MOV A,H",
            "MOV A,L",
            "MOV A,M",
            "MOV A,A",
            "ADD B",
            "ADD C",
            "ADD D",
            "ADD E",
            "ADD H",
            "ADD L",
            "ADD M",
            "ADD A",
            "ADC B",
            "ADC C",
            "ADC D",
            "ADC E",
            "ADC H",
            "ADC L",
            "ADC M",
            "ADC A",
            "SUB B",
            "SUB C",
            "SUB D",
            "SUB E",
            "SUB H",
            "SUB L",
            "SUB M",
            "SUB A",
            "SBB B",
            "SBB C",
            "SBB D",
            "SBB E",
            "SBB H",
            "SBB L",
            "SBB M",
            "SBB A",
            "ANA B",
            "ANA C",
            "ANA D",
            "ANA E",
            "ANA H",
            "ANA L",
            "ANA M",
            "ANA A",
            "XRA B",
            "XRA C",
            "XRA D",
            "XRA E",
            "XRA H",
            "XRA L",
            "XRA M",
            "XRA A",
            "ORA B",
            "ORA C",
            "ORA D",
            "ORA E",
            "ORA H",
            "ORA L",
            "ORA M",
            "ORA A",
            "CMP B",
            "CMP C",
            "CMP D",
            "CMP E",
            "CMP H",
            "CMP L",
            "CMP M",
            "CMP A",
            "RNZ",
            "POP B",
            "JNZ #A",
            "JMP #A",
            "CNZ #A",
            "PUSH B",
            "ADI #d",
            "RST 0",
            "RZ",
            "RET",
            "JZ #A",
            "UNK",
            "CZ #A",
            "CALL #A",
            "ACI #d",
            "RST 1",
            "RNC",
            "POP D",
            "JNC #A",
            "OUT #d",
            "CNC #A",
            "PUSH D",
            "SUI #d",
            "RST 2",
            "RC",
            "UNK",
            "JC #A",
            "IN #d",
            "CC #A",
            "UNK",
            "SBI #d",
            "RST 3",
            "RPO",
            "POP H",
            "JPO #A",
            "XTHL",
            "CPO #A",
            "PUSH H",
            "ANI #d",
            "RST 4",
            "RPE",
            "PCHL",
            "JPE #A",
            "XCHG",
            "CPE #A",
            "UNK",
            "XRI #d",
            "RST 5",
            "RP",
            "POP PSW",
            "JP #A",
            "DI",
            "CP #A",
            "PUSH PSW",
            "ORI #d",
            "RST 6",
            "RM",
            "SPHL",
            "JM #A",
            "EI",
            "CM #A",
            "UNK",
            "CPI #d",
            "RST 7"
    };
}