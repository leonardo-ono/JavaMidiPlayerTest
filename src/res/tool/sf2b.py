# https://pypi.org/project/sf2utils/
# codigo fonte https://gitlab.com/zeograd/sf2utils/-/tree/master/sf2utils?ref_type=heads

# sf2 refs:
# http://ipta.demokritos.gr/erl/sf2.html
# envelope:
# https://www.polyphone-soundfonts.com/forum/soundfonts-help/595-envelope-editor
    

from sf2utils.sf2parse import Sf2File
from sf2utils.sf2parse import Sf2Sample
from sf2utils.sf2parse import Sf2Instrument

import wave

def export_sample_to_wav(sample : Sf2Sample, output_path):
    with open(output_path, 'wb') as wav_file:
        wav_file = wave.open(output_path, 'w')
        wav_file.setnchannels(1)
        wav_file.setsampwidth(sample.sample_width)
        wav_file.setframerate(sample.sample_rate)
        wav_file.writeframes(sample.raw_sample_data)


def extract_sample(sample : Sf2Sample, base_note : int):
    sample_name = sample.name + ".wav"
    sample_name = sample_name.replace(':', '_')
    sample_name = sample_name.replace('/', '_')
    sample_name = sample_name.replace('*', '_')
    print("sample_filename=", sample_name)
    print("sample_length=", sample.duration)
    print("sample_width=", sample.sample_width)
    print("sample_rate=", sample.sample_rate)
    print("sample_is_mono=", sample.is_mono)
    print("sample_root_key=", base_note)
    print("sample_start_loop=", sample.start_loop)
    print("sample_end_loop=", sample.end_loop)

instrument_attack = 0.001
instrument_decay = 0.001
instrument_hold = 0.001
instrument_sustain = 0.001
instrument_release = 0.001
instrument_sample = None
instrument_base_note = 0

def reset_instrument_values():
    global instrument_attack
    global instrument_decay
    global instrument_hold
    global instrument_sustain
    global instrument_release
    global instrument_sample
    global instrument_base_note
    instrument_attack = 0.001
    instrument_decay = 0.001
    instrument_hold = 0.001
    instrument_sustain = 0.001
    instrument_release = 0.001
    instrument_sample = None
    instrument_base_note = 0

def extract_envelope(bag):
    global instrument_attack
    global instrument_decay
    global instrument_hold
    global instrument_sustain
    global instrument_release
    if bag.volume_envelope_attack:
        instrument_attack = bag.volume_envelope_attack
    if bag.volume_envelope_decay:
        instrument_decay = bag.volume_envelope_decay
    if bag.volume_envelope_hold:
        instrument_hold = bag.volume_envelope_hold
    if bag.volume_envelope_sustain:
        instrument_sustain = bag.volume_envelope_sustain
    if bag.volume_envelope_release:
        instrument_release = bag.volume_envelope_release

def db_to_linear(volume_db):
    linear_volume = 10.0 ** (volume_db / 20.0)
    return linear_volume

def extract_instrument(instrument_number, instrument : Sf2Instrument):
    global instrument_sample
    global instrument_base_note

    #if (instrument_number == 31):
    #    print("")

    reset_instrument_values()

    for index, bag in enumerate(instrument.bags):
        extract_envelope(bag)
        for bag_id, bag_gen in bag.gens.items():
            if bag_id == 53: # sample
                if not instrument_sample:
                    instrument_sample = bag.sample
                    instrument_base_note = bag.base_note if bag.base_note else bag.sample.original_pitch
                
                elif hasattr(bag, "base_note") and bag.base_note and bag.base_note >= 50 and bag.base_note <= 70:
                    instrument_sample = bag.sample
                    instrument_base_note = bag.base_note

                elif not bag.base_note and bag.sample.original_pitch >= 50 and bag.sample.original_pitch <= 70:
                    instrument_sample = bag.sample
                    instrument_base_note = bag.sample.original_pitch

    if instrument_base_note == 0:
        print("base note == 0!")

    print("instrument_number=", instrument_number)
    print("instrument_name=", instrument.name)
    print("envelope_attack=", instrument_attack)
    print("envelope_decay=", instrument_decay)
    print("envelope_hold=", instrument_hold)
    #print("sustain (db)=", instrument_sustain)
    print("envelope_sustain_level=", "{:.15f}".format(db_to_linear(-instrument_sustain)))
    print("envelope_release=", instrument_release)
    #print("===>sample=", bag.sample)
    
    if instrument_sample == None:
        print("sample not available !")

    extract_sample(instrument_sample, instrument_base_note)

    sample_name = instrument_sample.name + ".wav"
    sample_name = sample_name.replace(':', '_')
    sample_name = sample_name.replace('/', '_')
    sample_name = sample_name.replace('*', '_')
    export_sample_to_wav(instrument_sample, "samples2/" + sample_name)


def main():
    #arquivo_sf2 = 'GMGSx(Public-domain)(Kenneth-Rundt).sf2'

    # http://ntonyx.com/05_sf_01.html#008
    # license: "Free of charge for any usage. Unless in any case of distribution the mention of our URL: http://ntonyx.com would be highly appreciated. (...)"
    arquivo_sf2 = '32MbGMStereo.sf2'

    #arquivo_sf2 = 'Florestan-Basic-GM-GS-by-Nando-Florestan(Public-Domain).sf2'
    #arquivo_sf2 = 'Unison(Public-Domain).sf2'
    #arquivo_sf2 = 'Musica_Theoria_v2_(GM)(Public-Domain).sf2'

    # Carregar o arquivo SF2
    with open(arquivo_sf2, 'rb') as f:
        sf2 = Sf2File(f)

    for index, preset in enumerate(sf2.presets):
        #preset = sf2.presets[137]
        if hasattr(preset, 'bank') and hasattr(preset, 'preset') and preset.bank == 0:

            print("# preset=", preset.bank, ":", preset.preset, "-", preset.name, "-")
            for bag in preset.bags:
                #print("bag ", bag)
                for g in bag.gens:
                    #print("gen ", g)
                    if g == 41:
                        instrument_id = bag.gens.get(41).amount # g.am preset.bags[0].gens.get(41).amount

            instrument = sf2.instruments[instrument_id]
            #print("===> instrument ", instrument)

            if preset.preset == 66:
                print("instrument")

            extract_instrument(preset.preset, instrument)

    #instrument = sf2.instruments[130]
    #extract_instrument(instrument)

if __name__ == "__main__":
    main()
