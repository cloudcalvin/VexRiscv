package VexRiscv.demo

import VexRiscv.Plugin._
import VexRiscv.ip.{DataCacheConfig, InstructionCacheConfig}
import VexRiscv.{Plugin, VexRiscv, VexRiscvConfig}
import _root_.VexRiscv.coprocessor.{CustomInstrPlugin, VexRiscvCoProcessor, VexRiscvCoProcessorConfig}
import spinal.core._

/**
 * Created by spinalvm on 15.06.17.
 */
object GenFull extends App{
  def cpu() = new VexRiscv(
    config = VexRiscvConfig(
      plugins = List(
        new PcManagerSimplePlugin(
          resetVector = 0x00000000l,
          fastPcCalculation = false
        ),
        new IBusCachedPlugin(
          config = InstructionCacheConfig(
            cacheSize = 4096,
            bytePerLine =32,
            wayCount = 1,
            wrappedMemAccess = true,
            addressWidth = 32,
            cpuDataWidth = 32,
            memDataWidth = 32,
            catchIllegalAccess = true,
            catchAccessFault = true,
            catchMemoryTranslationMiss = true,
            asyncTagMemory = false,
            twoStageLogic = true
          ),
          askMemoryTranslation = true,
          memoryTranslatorPortConfig = MemoryTranslatorPortConfig(
            portTlbSize = 4
          )
        ),
        new DBusCachedPlugin(
          config = new DataCacheConfig(
            cacheSize         = 4096,
            bytePerLine       = 32,
            wayCount          = 1,
            addressWidth      = 32,
            cpuDataWidth      = 32,
            memDataWidth      = 32,
            catchAccessError  = true,
            catchIllegal      = true,
            catchUnaligned    = true,
            catchMemoryTranslationMiss = true
          ),
          memoryTranslatorPortConfig = MemoryTranslatorPortConfig(
            portTlbSize = 6
          )
        ),
        new MemoryTranslatorPlugin(
          tlbSize = 64,
          virtualRange = _(31 downto 28) === 0xC,
          ioRange      = _(31 downto 28) === 0xF
        ),
        new DecoderSimplePlugin(
          catchIllegalInstruction = true
        ),
        new RegFilePlugin(
          regFileReadyKind = Plugin.SYNC,
          zeroBoot = true
        ),
        new IntAluPlugin,
        new SrcPlugin(
          separatedAddSub = false,
          executeInsertion = true
        ),
        new FullBarrielShifterPlugin,
        new HazardSimplePlugin(
          bypassExecute           = true,
          bypassMemory            = true,
          bypassWriteBack         = true,
          bypassWriteBackBuffer   = true,
          pessimisticUseSrc       = false,
          pessimisticWriteRegFile = false,
          pessimisticAddressMatch = false
        ),
        new MulPlugin,
        new DivPlugin,
        new CsrPlugin(CsrPluginConfig.small),
        new DebugPlugin(ClockDomain.current.clone(reset = Bool().setName("debugReset"))),
        new BranchPlugin(
          earlyBranch = false,
          catchAddressMisaligned = true,
          prediction = DYNAMIC
        ),
        new YamlPlugin("cpu0.yaml")
      )
    )
  )

  SpinalVhdl(cpu())
}
