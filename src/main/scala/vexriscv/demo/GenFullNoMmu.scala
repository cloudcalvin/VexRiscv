package vexriscv.demo

import vexriscv.plugin._
import vexriscv.ip.{DataCacheConfig, InstructionCacheConfig}
import vexriscv.{plugin, VexRiscv, VexRiscvConfig}
import spinal.core._

/**
 * Created by spinalvm on 15.06.17.
 */
object GenFullNoMmu extends App{
  def cpu() = new VexRiscv(
    config = VexRiscvConfig(
      plugins = List(
        new PcManagerSimplePlugin(
          resetVector = 0x00000000l,
          relaxedPcCalculation = false
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
          )
        ),
        new StaticMemoryTranslatorPlugin(
          ioRange      = _(31 downto 28) === 0xF
        ),
        new DecoderSimplePlugin(
          catchIllegalInstruction = true
        ),
        new RegFilePlugin(
          regFileReadyKind = plugin.SYNC,
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
          prediction = STATIC
        ),
        new YamlPlugin("cpu0.yaml")
      )
    )
  )

  SpinalVerilog(cpu())
}
